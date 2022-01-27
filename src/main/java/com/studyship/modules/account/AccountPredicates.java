package com.studyship.modules.account;

import com.querydsl.core.types.Predicate;
import com.studyship.modules.study.Study;
import com.studyship.modules.tag.Tag;
import com.studyship.modules.zone.Zone;

import java.util.Set;

public class AccountPredicates {

    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        QAccount account = QAccount.account;
        return account.zones.any().in(zones).or(account.tags.any().in(tags));
    }
}
