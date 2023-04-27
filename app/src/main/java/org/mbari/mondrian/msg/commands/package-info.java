package org.mbari.mondrian.msg.commands;

/**
 * Commands encapsulate some bit of excutable code. Apply is used to execute it. Unapply should undo the
 * execution. These commands should be sent via the {@link org.mbari.imgfx.etc.rx.EventBus} and
 * pickec up and executed by the
 */