package com.streamerui.partnership;

import com.streamerui.repository.jpa.PartnershipEntity;
import com.streamerui.repository.jpa.PartnershipJpaRepository;
import com.streamerui.repository.jpa.StreamerJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("mysql")
public class JpaPartnerVisibilityService implements PartnerVisibilityService {

    private final PartnershipJpaRepository partnerships;
    private final StreamerJpaRepository streamers;

    public JpaPartnerVisibilityService(PartnershipJpaRepository partnerships, StreamerJpaRepository streamers) {
        this.partnerships = partnerships;
        this.streamers = streamers;
    }

    @Override
    public List<PartnerInfo> acceptedPartners(Long streamerId) {
        return partnerships.findAcceptedInvolving(streamerId).stream()
                .map((PartnershipEntity p) -> p.otherStreamerId(streamerId))
                .map(otherId -> new PartnerInfo(otherId,
                        streamers.findById(otherId).map(s -> s.getDisplayName()).orElse("Unknown streamer")))
                .collect(Collectors.toList());
    }
}
