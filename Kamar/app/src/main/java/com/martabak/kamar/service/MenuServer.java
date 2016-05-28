package com.martabak.kamar.service;

import android.content.Context;

import com.martabak.kamar.domain.Consumable;
import com.martabak.kamar.domain.permintaan.Permintaan;
import com.martabak.kamar.service.response.PostResponse;
import com.martabak.kamar.service.response.PutResponse;
import com.martabak.kamar.service.response.ViewResponse;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Exposes {@link MenuService}.
 */
public class MenuServer extends Server {

    /**
     * The singleton instance.
     */
    private static MenuServer instance;

    /**
     * The service api conf.
     */
    private MenuService service;

    /**
     * Constructor.
     */
    private MenuServer(Context c) {
        super(c);
        service = createService(MenuService.class);
    }

    /**
     * Obtains singleton instance.
     * @return The singleton instance.
     */
    public static MenuServer getInstance(Context c) {
        if (instance == null)
            instance = new MenuServer(c);
        return instance;
    }

    /**
     * Get all the menu items that match the states given.
     * I.E. {@code getMenuBySection("FOOD", "DESSERT", "BEVERAGES")}
     * @param sections The sections to match on.
     * @return Observable on the consumable menu items.
     */
    public Observable<Consumable> getMenuBySection(String... sections) {
        List<Observable<Consumable>> results = new ArrayList<>(sections.length);
        for (String section : sections) {
            results.add(service.getMenuBySection('"' + section + '"')
                    .flatMap(new Func1<ViewResponse<Consumable>, Observable<Consumable>>() {
                        @Override
                        public Observable<Consumable> call(ViewResponse<Consumable> response) {
                            List<Consumable> consumables = new ArrayList<>(response.total_rows);
                            for (ViewResponse<Consumable>.ViewResult<Consumable> i : response.rows) {
                                consumables.add(i.value);
                            }
                            return Observable.from(consumables);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()));
        }
        return Observable.merge(results);
    }

}