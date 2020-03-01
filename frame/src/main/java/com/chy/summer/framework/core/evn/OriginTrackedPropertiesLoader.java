package com.chy.summer.framework.core.evn;

import com.chy.summer.framework.boot.origin.Origin;
import com.chy.summer.framework.boot.origin.OriginTrackedValue;
import com.chy.summer.framework.boot.origin.TextResourceOrigin;
import com.chy.summer.framework.core.io.support.Resource;
import com.chy.summer.framework.util.Assert;
import com.chy.summer.framework.boot.origin.TextResourceOrigin.Location;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 把配置文件加载进map里面的类
 */
public class OriginTrackedPropertiesLoader {

    private final Resource resource;


    OriginTrackedPropertiesLoader(Resource resource) {
        Assert.notNull(resource, "Resource must not be null");
        this.resource = resource;
    }

    /**
     * 加载配置文件,把值都存成 map的形式
     * @param expandLists 如果是数组的值是否拆分 比如  mykey[]=a,b,c ---> mykey[0]=a,mykey[0]=b,mykey[0]=c
     * @return
     * @throws IOException
     */
    Map<String, OriginTrackedValue> load(boolean expandLists) throws IOException {
        //加载了流,读取文件
        try (CharacterReader reader = new CharacterReader(this.resource)) {
            Map<String, OriginTrackedValue> result = new LinkedHashMap<>();
            //这个buff是用来加载key的,复用同一个内存空间
            StringBuilder keyBuff = new StringBuilder();
            while (reader.read()) {
                //加载了key
                String key = loadKey(keyBuff, reader).trim();
                //如果是[]结尾的 那么就认为是数组 mykey[]=a,b,c ---> mykey[0]=a,mykey[0]=b,mykey[0]=c
                if (expandLists && key.endsWith("[]")) {
                    key = key.substring(0, key.length() - 2);
                    int index = 0;
                    do {
                        OriginTrackedValue value = loadValue(keyBuff, reader, true);
                        put(result, key + "[" + (index++) + "]", value);
                        if (!reader.isEndOfLine()) {
                            reader.read();
                        }
                    }
                    while (!reader.isEndOfLine());
                }
                else {
                    OriginTrackedValue value = loadValue(keyBuff, reader, false);
                    put(result, key, value);
                }
            }
            return result;
        }
    }

    private void put(Map<String, OriginTrackedValue> result, String key, OriginTrackedValue value) {
        if (!key.isEmpty()) {
            result.put(key, value);
        }
    }

    /**
     * 用了读取 key值
     * @param buffer
     * @param reader
     * @return
     * @throws IOException
     */
    private String loadKey(StringBuilder buffer, CharacterReader reader) throws IOException {
        //把buffer清空
        buffer.setLength(0);
        boolean previousWhitespace = false;
        while (!reader.isEndOfLine()) {
            //如果读的时候遇到了 分隔符号 就是 =号,就可以准备结束了
            if (reader.isPropertyDelimiter()) {
                //指针走下一位,其实就是走到等号后面一位了
                reader.read();
                return buffer.toString();
            }

            //如果上面一位是空格,但是当前这位不是空格, 说明有问题呀,直接把用空格分割的字符给返回了 aaa bbb/n -> aaa /n
            if (!reader.isWhiteSpace() && previousWhitespace) {
                return buffer.toString();
            }
            //当前读到的这个字符如果是个 空格就记录一下
            previousWhitespace = reader.isWhiteSpace();
            //把当前读到的这个字符给拼到 buff上面
            buffer.append(reader.getCharacter());
            //指针走下面一位
            reader.read();
        }
        return buffer.toString();
    }

    /**
     * 读取 value值
     * @param buffer
     * @param reader
     * @param splitLists
     * @return
     * @throws IOException
     */
    private OriginTrackedValue loadValue(StringBuilder buffer, CharacterReader reader, boolean splitLists)
            throws IOException {
        buffer.setLength(0);
        while (reader.isWhiteSpace() && !reader.isEndOfLine()) {
            reader.read();
        }
        Location location = reader.getLocation();
        while (!reader.isEndOfLine() && !(splitLists && reader.isListDelimiter())) {
            buffer.append(reader.getCharacter());
            reader.read();
        }
        //这个 location 是用了记录 这个value对应的配置文件的位置在哪里 比如 23:15
        Origin origin = new TextResourceOrigin(this.resource, location);
        return OriginTrackedValue.of(buffer.toString(), origin);
    }


    private static class CharacterReader implements Closeable {

        private static final String[] ESCAPES = { "trnf", "\t\r\n\f" };

        private final LineNumberReader reader;

        private int columnNumber = -1;

        private boolean escaped;

        private int character;

        CharacterReader(Resource resource) throws IOException {
            this.reader = new LineNumberReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.ISO_8859_1));
        }

        @Override
        public void close() throws IOException {
            this.reader.close();
        }

        boolean read() throws IOException {
            return read(false);
        }

        boolean read(boolean wrappedLine) throws IOException {
            this.escaped = false;
            this.character = this.reader.read();
            this.columnNumber++;
            if (this.columnNumber == 0) {
                skipLeadingWhitespace();
                if (!wrappedLine) {
                    skipComment();
                }
            }
            if (this.character == '\\') {
                this.escaped = true;
                readEscaped();
            }
            else if (this.character == '\n') {
                this.columnNumber = -1;
            }
            return !isEndOfFile();
        }

        private void skipLeadingWhitespace() throws IOException {
            while (isWhiteSpace()) {
                this.character = this.reader.read();
                this.columnNumber++;
            }
        }

        private void skipComment() throws IOException {
            if (this.character == '#' || this.character == '!') {
                while (this.character != '\n' && this.character != -1) {
                    this.character = this.reader.read();
                }
                this.columnNumber = -1;
                read();
            }
        }

        private void readEscaped() throws IOException {
            this.character = this.reader.read();
            int escapeIndex = ESCAPES[0].indexOf(this.character);
            if (escapeIndex != -1) {
                this.character = ESCAPES[1].charAt(escapeIndex);
            }
            else if (this.character == '\n') {
                this.columnNumber = -1;
                read(true);
            }
            else if (this.character == 'u') {
                readUnicode();
            }
        }

        private void readUnicode() throws IOException {
            this.character = 0;
            for (int i = 0; i < 4; i++) {
                int digit = this.reader.read();
                if (digit >= '0' && digit <= '9') {
                    this.character = (this.character << 4) + digit - '0';
                }
                else if (digit >= 'a' && digit <= 'f') {
                    this.character = (this.character << 4) + digit - 'a' + 10;
                }
                else if (digit >= 'A' && digit <= 'F') {
                    this.character = (this.character << 4) + digit - 'A' + 10;
                }
                else {
                    throw new IllegalStateException("Malformed \\uxxxx encoding.");
                }
            }
        }

        boolean isWhiteSpace() {
            return !this.escaped && (this.character == ' ' || this.character == '\t' || this.character == '\f');
        }

        boolean isEndOfFile() {
            return this.character == -1;
        }

        boolean isEndOfLine() {
            return this.character == -1 || (!this.escaped && this.character == '\n');
        }

        boolean isListDelimiter() {
            return !this.escaped && this.character == ',';
        }

        boolean isPropertyDelimiter() {
            return !this.escaped && (this.character == '=' || this.character == ':');
        }

        char getCharacter() {
            return (char) this.character;
        }

        Location getLocation() {
            return new Location(this.reader.getLineNumber(), this.columnNumber);
        }



    }
}
