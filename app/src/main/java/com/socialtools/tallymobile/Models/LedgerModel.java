package com.socialtools.tallymobile.Models;

import java.io.Serializable;

public class LedgerModel implements Serializable {

    public String
            ledgerId,
            ledgerName,
            ledgerAlias,
            ledgerParent,
            ledgerAddress_1,
            ledgerAddress_2,
            ledgerAddress_3,
            state_name,
            country_name,
            gst,
            pan,
            createAt,
            creatorId;

    public Boolean isSynced;
}
