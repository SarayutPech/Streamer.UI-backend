package com.streamerui.partnership;

import java.util.List;

/**
 * Who a streamer's accepted partners are, for composing "my badges/banners
 * plus my partners'" lists (see BadgeLibraryController/BannerLibraryController).
 * Local/single-tenant dev mode has no partnership concept at all - see
 * NoPartnersVisibilityService.
 */
public interface PartnerVisibilityService {
    List<PartnerInfo> acceptedPartners(Long streamerId);
}
