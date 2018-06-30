package com.rsh.coviewer.bean.celebrity;

import com.rsh.coviewer.bean.OneSubject;
import lombok.Data;

import java.util.ArrayList;

/**
 * Created by wsk1103 on 2017/10/22.
 */
@Data
public class Work {
    private ArrayList<String> roles;
    private OneSubject subject;
}
