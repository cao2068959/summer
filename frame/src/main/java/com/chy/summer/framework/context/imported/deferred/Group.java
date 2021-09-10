package com.chy.summer.framework.context.imported.deferred;


import com.chy.summer.framework.core.type.AnnotationMetadata;

public interface Group {


    void process(AnnotationMetadata metadata, DeferredImportSelector selector);

    Iterable<Entry> selectImports();


    class Entry {

        private final AnnotationMetadata metadata;

        private final String importClassName;

        public Entry(AnnotationMetadata metadata, String importClassName) {
            this.metadata = metadata;
            this.importClassName = importClassName;
        }


        public AnnotationMetadata getMetadata() {
            return this.metadata;
        }


        public String getImportClassName() {
            return this.importClassName;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            Entry entry = (Entry) other;
            return (this.metadata.equals(entry.metadata) && this.importClassName.equals(entry.importClassName));
        }

        @Override
        public int hashCode() {
            return (this.metadata.hashCode() * 31 + this.importClassName.hashCode());
        }

        @Override
        public String toString() {
            return this.importClassName;
        }
    }
}
