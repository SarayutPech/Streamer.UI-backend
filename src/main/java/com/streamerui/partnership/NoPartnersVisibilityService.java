package com.streamerui.partnership;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/** Local/single-tenant dev mode: no accounts, so no partnerships either. */
@Service
@Profile("!mysql")
public class NoPartnersVisibilityService implements PartnerVisibilityService {
    @Override
    public List<PartnerInfo> acceptedPartners(Long streamerId) {
        return List.of();
    }
}
