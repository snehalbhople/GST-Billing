package com.csi.repository;

import com.csi.model.GSTBill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GSTBillRepo extends JpaRepository<GSTBill , String> {
}