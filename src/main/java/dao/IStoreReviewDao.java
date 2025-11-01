/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

import java.util.List;


/**
 *
 * @author LENOVO
 */
public interface IStoreReviewDao {
    List<model.StoreReview> findAll() throws Exception;

    public boolean insertReview(int customerId, int rating, String comment) ;
}