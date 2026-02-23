// HUD script (declarative + reusable helpers)
// Context passed from Java:
// - ctx.primaryButtonText
// - ctx.state (Map<String, Object>)

def S = { ctx -> (ctx?.state ?: [:]) as Map }

def btn = { String id, String label, String alignX, String alignY,
            Number x, Number y, Number w, Number h,
            boolean active = true, String pivot = null ->
    def row = [
            id    : id,
            label : label,
            alignX: alignX,
            alignY: alignY,
            x     : x,
            y     : y,
            width : w,
            height: h,
            scale : 1.0,
            active: active
    ]
    if (pivot != null) row.pivot = pivot
    row
}

[
        base   : { ctx ->
            def state = S(ctx)
            [
                    sprites: [
                            [texture: 'char-frame', pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'TOP', x: 0, y: 18, scale: 2.0],
                            [texture: 'separator', pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'BOTTOM', x: 0, y: -28, scale: 2.0]
                    ],
                    texts  : [
                            [text: 'unit.name', pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'TOP', x: 10, y: 2, scale: 1.0]
                    ],
                    buttons: [
                            btn('primary-button', (ctx?.primaryButtonText ?: ''), 'LEFT', 'TOP', 10, 170, 100, 28, true, 'screen.left.top'),
                            btn('debug-button', 'deb...ug', 'LEFT', 'TOP', 10, 204, 100, 28, state.debugMode == true, 'screen.left.top')
                    ]
            ]
        },
        dialogs: [
                'main.menu'    : { ctx ->
                    [
                            texts  : [
                                    [text: '^2DotRuby^0\nMain Menu', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 20, scale: 1.0],
                                    [text: 'Choose a runtime session', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 62, scale: 1.0]
                            ],
                            buttons: [
                                    btn('start-session', 'start session', 'CENTER', 'TOP', 0, 102, 220, 28),
                                    btn('open-showcase', 'showcase session', 'CENTER', 'TOP', 0, 136, 220, 28),
                                    btn('exit-app', 'exit', 'CENTER', 'TOP', 0, 170, 220, 28)
                            ]
                    ]
                },
                'session.pause': { ctx ->
                    [
                            texts  : [
                                    [text: '^3session paused', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 18, scale: 1.0]
                            ],
                            buttons: [
                                    btn('to-main-menu', 'main menu', 'CENTER', 'TOP', 0, 78, 210, 28),
                                    btn('close', 'close', 'CENTER', 'TOP', 0, 112, 210, 28)
                            ]
                    ]
                },
                'showcase.menu' : { ctx ->
                    [
                            texts  : [
                                    [text: '^2Showcase Session^0', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 16, scale: 1.0],
                                    [text: 'Dialog flags + text preview', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 38, scale: 1.0]
                            ],
                            buttons: [
                                    btn('showcase-open-none', 'dialog: none', 'CENTER', 'TOP', 0, 74, 220, 24),
                                    btn('showcase-open-cursor-lock', 'dialog: lock cursor', 'CENTER', 'TOP', 0, 102, 220, 24),
                                    btn('showcase-open-edge-block', 'dialog: block edge', 'CENTER', 'TOP', 0, 130, 220, 24),
                                    btn('showcase-open-all', 'dialog: all flags', 'CENTER', 'TOP', 0, 158, 220, 24),
                                    btn('showcase-main-menu', 'main menu', 'CENTER', 'TOP', 0, 190, 220, 24)
                            ]
                    ]
                },
                'debug.info'   : { ctx ->
                    def state = S(ctx)
                    [
                            texts  : [],
                            buttons: [
                                    btn('toggle-rendering', 'rendering', 'CENTER', 'TOP', 0, 14, 180, 24, state.showRendering != false),
                                    btn('toggle-camera', 'camera', 'CENTER', 'TOP', 0, 42, 180, 24, state.showCamera != false),
                                    btn('toggle-time', 'time', 'CENTER', 'TOP', 0, 70, 180, 24, state.showTime != false),
                                    btn('toggle-client', 'client', 'CENTER', 'TOP', 0, 98, 180, 24, state.showClient != false),
                                    btn('toggle-all', 'all', 'LEFT', 'BOTTOM', 12, -10, 84, 22,
                                            (state.showRendering != false && state.showCamera != false && state.showTime != false && state.showClient != false)),
                                    btn('close', 'close', 'RIGHT', 'BOTTOM', -12, -10, 96, 22)
                            ]
                    ]
                }
        ]
]
