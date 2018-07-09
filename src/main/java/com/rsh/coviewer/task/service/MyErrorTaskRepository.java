package com.rsh.coviewer.task.service;

import com.rsh.coviewer.task.entity.MytaskerrorEntity;
import com.rsh.coviewer.task.entity.MytaskerrorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @DESCRIPTION :错误任务记录
 * @AUTHOR : WuShukai1103
 * @TIME : 2018/1/24  22:43
 */
public interface MyErrorTaskRepository extends JpaRepository<MytaskerrorEntity, Integer> {
}
