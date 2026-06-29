package com.dabashou.system.api;

import java.util.List;

public interface SystemApi {

    List<String> getRoleCodesByUserId(Long userId);
}
