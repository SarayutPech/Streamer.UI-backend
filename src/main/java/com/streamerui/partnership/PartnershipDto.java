package com.streamerui.partnership;

/** One partnership row from the current streamer's point of view. */
public class PartnershipDto {
    private Long id;
    private Long otherStreamerId;
    private String otherDisplayName;
    /** "incoming" (they requested you), "outgoing" (you requested them). */
    private String direction;
    private String status;

    public PartnershipDto() {
    }

    public PartnershipDto(Long id, Long otherStreamerId, String otherDisplayName, String direction, String status) {
        this.id = id;
        this.otherStreamerId = otherStreamerId;
        this.otherDisplayName = otherDisplayName;
        this.direction = direction;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOtherStreamerId() {
        return otherStreamerId;
    }

    public void setOtherStreamerId(Long otherStreamerId) {
        this.otherStreamerId = otherStreamerId;
    }

    public String getOtherDisplayName() {
        return otherDisplayName;
    }

    public void setOtherDisplayName(String otherDisplayName) {
        this.otherDisplayName = otherDisplayName;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
