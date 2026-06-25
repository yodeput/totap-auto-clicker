package com.kunnn.totap.core.domain.model

/**
 * The persistent shape of a preset (spec glossary): mode + targets + the global
 * anti-detection toggle. This is what gets saved/loaded and synced.
 *
 * @property mode           how targets are ordered in time.
 * @property targets        the ordered list of target points. Must be non-empty.
 * @property antiDetection  master switch; when true, jitter (per-target or
 *                          global default) is applied to every fire.
 */
data class ClickConfig(
    val mode: ClickMode = ClickMode.SINGLE,
    val targets: List<Target>,
    val antiDetection: Boolean = false,
) {
    init {
        require(targets.isNotEmpty()) { "ClickConfig must have at least one target" }
    }
}
