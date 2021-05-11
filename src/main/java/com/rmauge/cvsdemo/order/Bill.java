package com.rmauge.cvsdemo.order;

public class Bill {
    private Long id;
    private Double total;

    public Bill(Long id, Double total) {
        this.id = id;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(final Double total) {
        this.total = total;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Bill{");
        sb.append("id=").append(id);
        sb.append(", total=").append(total);
        sb.append('}');
        return sb.toString();
    }
}
