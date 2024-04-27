package com.oksusu.susu.cache.model

import com.oksusu.susu.cache.model.vo.OidcPublicKeyCacheModel

data class OidcPublicKeysCacheModel(
    val keys: List<OidcPublicKeyCacheModel>,
)
