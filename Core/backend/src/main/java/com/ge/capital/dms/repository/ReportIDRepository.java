package com.ge.capital.dms.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.entity.ReportID;

@Component
@Transactional
public interface ReportIDRepository extends CrudRepository<ReportID, String> {

}
