package com.socialtools.tallymobile.Models;

import java.io.Serializable;

public class ItemModel implements Serializable {

    public String
            itemId,
            itemName,
            itemDescription,
            itemPartnumber,
            itemUom,
            itemHsncode,
            itemMrp,
            itemPp,
            itemSp,
            itemGst,
            createAt,
            alterAt,
            alterId,
            creatorId;

    public Boolean isSynced;
}
