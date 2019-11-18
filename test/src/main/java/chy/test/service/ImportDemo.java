package chy.test.service;

import chy.test.annotation.ImportC;
import com.chy.summer.framework.context.annotation.Configuration;
import com.chy.summer.framework.context.annotation.Import;

@Configuration
@Import(ImprotExec1.class)
@ImportC
public class ImportDemo {
}
