package issues.lhg142;

public class Page {
    private Long offset;
    private Long size;

    public Page(Long offset, Long size) {
        this.offset = offset;
        this.size = size;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
