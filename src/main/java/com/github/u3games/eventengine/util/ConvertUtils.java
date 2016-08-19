package com.github.u3games.eventengine.util;

import com.github.u3games.eventengine.model.EItemHolder;
import com.github.u3games.eventengine.model.ELocation;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.holders.ItemHolder;

import java.util.ArrayList;
import java.util.List;

public class ConvertUtils {

    public static List<Location> convertToListLocations(List<ELocation> list) {
        List<Location> locations = new ArrayList<>();
        for (ELocation loc : list) {
            locations.add(new Location(loc.getX(), loc.getY(), loc.getZ()));
        }

        return locations;
    }

    public static List<ItemHolder> convertToListItemsHolders(List<EItemHolder> list) {
        List<ItemHolder> items = new ArrayList<>();
        for (EItemHolder item : list) {
            items.add(new ItemHolder(item.getId(), item.getAmount()));
        }

        return items;
    }
}
