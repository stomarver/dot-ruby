[
        base: { ctx ->
            def state = (ctx?.state ?: [:]) as Map
            [
                    sprites: [
                            [texture: 'char-frame', pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'TOP', x: 0, y: 18, scale: 2.0],
                            [texture: 'separator', pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'BOTTOM', x: 0, y: -28, scale: 2.0]
                    ],
                    texts  : [
                            [text: 'unit.name', pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'TOP', x: 10, y: 2, scale: 1.0]
                    ],
                    buttons: [
                            [id: 'primary-button', label: (ctx?.primaryButtonText ?: ''), pivot: 'screen.left.top', alignX: 'LEFT', alignY: 'TOP', x: 10, y: 170, width: 100, height: 28, scale: 1.0, active: true]
                    ]
            ]
        },
        dialogs: [
                'main.menu' : { ctx ->
                    [
                            texts  : [
                                    [text: '^2DotRuby^0\nMain Menu', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 20, scale: 1.0],
                                    [text: 'Use buttons to start or exit', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 62, scale: 1.0]
                            ],
                            buttons: [
                                    [id: 'start-session', label: 'start session', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 108, width: 200, height: 28, active: true],
                                    [id: 'exit-app', label: 'exit', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 142, width: 200, height: 28, active: true]
                            ]
                    ]
                },
                'session.pause': { ctx ->
                    [
                            texts  : [
                                    [text: '^3session paused', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 18, scale: 1.0]
                            ],
                            buttons: [
                                    [id: 'to-main-menu', label: 'main menu', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 78, width: 210, height: 28, active: true],
                                    [id: 'close', label: 'close', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 112, width: 210, height: 28, active: true]
                            ]
                    ]
                },
                'debug.info': { ctx ->
                    def state = (ctx?.state ?: [:]) as Map
                    [
                            texts  : [],
                            buttons: [
                                    [id: 'toggle-rendering', label: 'rendering', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 14, width: 180, height: 24, active: state.showRendering != false],
                                    [id: 'toggle-camera', label: 'camera', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 42, width: 180, height: 24, active: state.showCamera != false],
                                    [id: 'toggle-time', label: 'time', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 70, width: 180, height: 24, active: state.showTime != false],
                                    [id: 'toggle-client', label: 'client', alignX: 'CENTER', alignY: 'TOP', x: 0, y: 98, width: 180, height: 24, active: state.showClient != false],
                                    [id: 'toggle-all', label: 'all', alignX: 'LEFT', alignY: 'BOTTOM', x: 12, y: -10, width: 84, height: 22,
                                     active: (state.showRendering != false && state.showCamera != false && state.showTime != false && state.showClient != false)],
                                    [id: 'close', label: 'close', alignX: 'RIGHT', alignY: 'BOTTOM', x: -12, y: -10, width: 96, height: 22, active: true]
                            ]
                    ]
                }
        ]
]
