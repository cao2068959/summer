package com.chy.summer.framework.boot.origin;

import com.chy.summer.framework.core.io.support.Resource;

public class TextResourceOrigin implements Origin {

    private final Resource resource;

    private final Location location;

    public TextResourceOrigin(Resource resource, Location location) {
        this.resource = resource;
        this.location = location;
    }


    /**
     * 用于记录读取位置的类
     */
    public static final class Location {

        private final int line;

        private final int column;


        public Location(int line, int column) {
            this.line = line;
            this.column = column;
        }


        public int getLine() {
            return this.line;
        }


        public int getColumn() {
            return this.column;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Location other = (Location) obj;
            boolean result = true;
            result = result && this.line == other.line;
            result = result && this.column == other.column;
            return result;
        }

        @Override
        public int hashCode() {
            return (31 * this.line) + this.column;
        }

        @Override
        public String toString() {
            return (this.line + 1) + ":" + (this.column + 1);
        }

    }
}
