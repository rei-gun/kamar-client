package com.martabak.kamar.domain.managers;

import com.martabak.kamar.domain.Consumable;
import com.martabak.kamar.domain.permintaan.RestaurantOrder;

import java.util.HashMap;
import java.util.List;

/**
 * Keeps the restaurant order in memory to be passed from RestaurantActivity to
 * RestaurantConfirmationActivity.
 */
public class RestaurantOrderManager implements Manager {

    private static RestaurantOrderManager instance;

    private RestaurantOrder order = null;

    private List<String> restaurantImgUrls = null;

    private List<Consumable> consumables = null;

    private RestaurantOrderManager() {}

    public static RestaurantOrderManager getInstance() {
        if (instance == null) {
            instance = new RestaurantOrderManager();
            Managers.register(instance);
        }
        return instance;
    }

    public void clear() {
        order = null;
        restaurantImgUrls = null;
        consumables = null;
    }

    /**
     * @return The current ic_restaurant order, if it has been set.
     */
    public RestaurantOrder getOrder() {
        return order;
    }

    /**
     * @return the URLs of selected menu items
     */
    public List<String> getUrls() { return restaurantImgUrls; }

    /**
     * Set the current ic_restaurant order.
     * @param order The current ic_restaurant order.
     */
    public void setOrder(RestaurantOrder order) {
        this.order = order;
    }

    /**
     * Set the URLs of selected menu items for RestaurantConfirmation
     * @param restaurantImgUrls
     */
    public void setUrls(List<String> restaurantImgUrls) {
        this.restaurantImgUrls = restaurantImgUrls;
    }

    /**
     * Save the consumables into memory
     * @param consumables
     */
    public void saveConsumables(List<Consumable> consumables) {
        this.consumables = consumables;
    }

    /**
     * @return the consumables
     */
    public List<Consumable> getConsumables() {
        return consumables;
    }

}
