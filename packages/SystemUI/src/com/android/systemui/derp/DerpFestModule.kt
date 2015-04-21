/*
 * Copyright (C) 2023 DerpFest
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.systemui.derp

import com.android.systemui.qs.tileimpl.QSTileImpl
import com.android.systemui.qs.tiles.CaffeineTile
import com.android.systemui.qs.tiles.HeadsUpTile

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
interface DerpFestModule {
    /** Inject CaffeineTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(CaffeineTile.TILE_SPEC)
    fun bindCaffeineTile(caffeineTile: CaffeineTile): QSTileImpl<*>

    /** Inject HeadsUpTile into tileMap in QSModule */
    @Binds
    @IntoMap
    @StringKey(HeadsUpTile.TILE_SPEC)
    fun bindHeadsUpTile(headsUpTile: HeadsUpTile): QSTileImpl<*>
}
