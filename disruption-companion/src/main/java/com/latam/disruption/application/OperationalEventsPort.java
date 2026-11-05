package com.latam.disruption.application;

import com.latam.disruption.domain.OperationalEvents;
import java.util.List;

public interface OperationalEventsPort {
    OperationalEvents getOperationalEvents(List<String> segmentIds);
}
