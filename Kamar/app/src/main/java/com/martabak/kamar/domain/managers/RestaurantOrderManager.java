package com.martabak.kamar.domain.managers;

import com.martabak.kamar.domain.Consumable;
import com.martabak.kamar.domain.permintaan.RestaurantOrder;

import java.util.HashMap;
import java.util.List;

/**
 * Keeps the ic_restaurant order in memory to be passed from RestaurantActivity to
 * RestaurantConfirmationActivity.
 */
public class RestaurantOrderManager {

    private static RestaurantOrderManager instance;

    private RestaurantOrder order = null;

    private List<String> restaurantImgUrls = null;

    private HashMap<String, Consumable> idToConsumables = null;

    private RestaurantOrderManager() {}

    public static RestaurantOrderManager getInstance() {
        if (instance == null) {
            instance = new RestaurantOrderManager();
        }
        return instance;
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
     * @param sectionToConsumables
     */
    public void saveConsumables(HashMap<String, Consumable> sectionToConsumables) {
        this.idToConsumables = sectionToConsumables;
    }

    /**
     * @return the consumables
     */
    public HashMap<String, Consumable> getConsumables() {
        return idToConsumables;
    }

}
