# Interactive Studio — referência Dev log #1

O vídeo público [Dev log #1, @pickzugng / Purxiy](https://www.tiktok.com/@pickzugng/video/7696192905899216160), enviado em https://vt.tiktok.com/ZSg9fdwP1/, foi baixado temporariamente e analisado quadro a quadro em 24/10/2026. Duração real: **29,2 segundos**. O original não é distribuído no repositório.

Esta revisão **0.6.0-alpha.1** acrescenta versões originais Alien dos controles e efeitos identificáveis. Continua em **Minecraft Java 1.8.9 / Forge 11.15.1.2318 / Java 8**; não exige migração. Branch `feature/interactive-studio-20261024`, empilhada sobre o PR #17 e preservando os PRs #15/#16/#17. Nenhum merge na main.

## Correspondência com o vídeo

| Recurso mostrado | Implementação Alien | Escopo real |
| --- | --- | --- |
| Interactive Main Menu / Grove Trees | Cenário 3D original com geometria e atlas de blocos do próprio Minecraft, árvores, flores e rio animado. Grove, Sakura e Alpine. | Diorama renderizado dentro do Forge, sem iniciar servidor ou carregar um save do jogador. Não usa o mapa/cerejeiras/assets Purxiy. |
| Time of day / weather / camera | Day, Sunset, Night; Clear, Rain, Snow, Mixed; Orbit/Fixed, arrastar para girar e scroll para aproximar. | Aplica-se ao cenário de menu. O céu/clima do mundo continuam nos editores existentes. |
| Multiplayer com fundo desfocado | Lista vanilla de servidores sobre o framebuffer real do menu com Gaussian blur. | Ping, adicionar/editar/remover e conectar continuam vanilla. Não inclui lista de servidores de terceiros ou conta externa. |
| Orbit / Click GUI | Navegação radial, galeria com busca, scroll, cards, switches reais, clique direito para abrir os editores. | Layout Gallery opcional; Panels e Studio permanecem disponíveis. Cada card adapta uma configuração real; não inventa os módulos ocultos do vídeo. |
| Config Library / detalhes | Presets locais Lilac/Night/Clean; salvar, carregar, duplicar, renomear, excluir, importar/exportar JSON e copiar JSON para compartilhar. Prévia calcula switches que ligam/desligam; lista categorias e configurações ativas. Foto salva o framebuffer atual. | Biblioteca offline. Não há Discover, conta, backend, upload ou compartilhamento por URL. Copiar/exportar gera dados reais para compartilhar manualmente. A foto é local e não é embutida no JSON. |
| Sky / oito estilos nomeados | Galaxy, Aether, Nocturne, Amethyst, Opaline, Rain, Snow, Fire; prévias reais dos shaders, quatro paletas, velocidade de animação, vivacidade, respiração e reset. | Presets procedurais originais, sem copiar shaders/arte Purxiy. Os oito céus anteriores continuam; Fire é compartilhado, totalizando 15 estilos + Off. Galaxy usa a composição Cosmic anterior como base. |
| Ambience | Reutiliza as partículas ambientes existentes do Particle Studio, com forma, cores, glow e orçamento reais. | Sem um segundo sistema de partículas duplicado. |
| Mist | Névoa local de distância e altura por reconstrução de profundidade; densidade configurável. | Sem alterar clima/fog/horário do servidor. |
| Afterglow | Emite pontos de luz que sobem e desaparecem no local de mortes reais de entidades visíveis no client. | Até 16 fontes / 3 segundos; inspeção limitada a 256 entidades carregadas por tick, alcance configurado. Não é ragdoll ou fragmentação do modelo. |
| Radiance | Composição luminosa das bordas da imagem do mundo e da mão/item antes do HUD. | Filtro de tela da cena inteira, não outline seletivo por entidade, PBR ou bloom físico. |
| Target ESP | Anel animado em torno da entidade viva acompanhada pelo Target HUD. | Somente alvo real, visível e no alcance; mantém profundidade/oclusão. Não rastreia jogadores através de paredes. |
| Bokeh | Desfoque de nove amostras, foco na profundidade do centro, rejeição de amostras de profundidade muito diferente. | Depth of field aproximado em tela; não simula abertura física, discos ópticos ou ray tracing. HUD continua nítido. |
| Hand Movement | Renderização real da mão/item em primeira pessoa com leve movimento, preservando projeção vanilla, bob, hurt, luz e overlays. Intensidade ajustável. | Usa o hook Forge de mão; não modifica ataques/uso de itens. Não foi testado junto de outros mods que substituam esse renderizador. |
| Name Tags | Nome e vida reais dos jogadores visíveis, limitados por distância/orçamento. | Sem dados de servidor inventados, wallhack ou tags de entidades invisíveis. Acima do orçamento continua a tag vanilla. |
| Zoom | Fator configurável; segurar **C** altera FOV pelo hook Forge. | Não altera sensibilidade salva, pacotes ou posição da câmera. O atalho C é fixo nesta revisão. |
| Shockwave, Glass, X-Ray, Cute World, crosshair e HUD | Preservados do Scene Studio / HUD Ultimate e acessíveis pela galeria. | Limites dos PRs anteriores continuam: X-Ray é esqueleto decorativo, Cute World são rostos, Glass não substitui mão/ícones em primeira pessoa. |

O vídeo exibe a contagem “25” no grupo Render e nomes de grupos, mas não abre todos esses módulos ou revela seus algoritmos. Esta entrega cobre o que é identificável na gravação, sem atribuir comportamento a cartões que não aparecem. A comparação de perfis conta switches locais e masters; não compara todas as opções numéricas, posições de widgets ou cores. O carregamento continua aplicando o perfil completo do Unified.

## Como acessar

- Menu inicial → **Scene controls** para cenário, hora, clima e câmera. Desligar “3D backdrop” recupera o menu anterior.
- **Module Gallery** → Render / Player / HUD / Combat. Clique esquerdo alterna; clique direito abre o editor relacionado. **Use this menu** escolhe Gallery para a tecla de menu; layouts antigos continuam nos Studios.
- **Orbit / Studios** abre os destinos radiais. **Config library** abre os perfis locais. Para duplicar/renomear/importar, digite um nome válido no campo superior antes de acionar a operação.
- **Effects Studio → Scene Studio → Sky styles** abre estilos e controles; **Choose accent** anterior foi preservado.
- **Living Sky** na galeria também abre o editor por clique direito. Ambience abre Particle Studio; HUD abre o editor existente; Combat mantém o guard e os módulos anteriores.

## Persistência e custos

`studio` e os novos campos `scenes` usam o mesmo JSON normalizado/limitado do Unified. Perfis antigos preservam seus widgets, cosméticos, mapas, temas, controles e modo de menu; novos efeitos opcionais começam desligados. O cenário de menu é uma preferência de apresentação independente, ligada por padrão e reversível.

A geometria estática do cenário tem cache em display list e invalidação no reload do atlas; água/clima/câmera são animados sem reconstruir o terreno por frame. O compositor reutiliza texturas/buffers e uniforms; Bokeh usa nove taps, Radiance cinco. Histórico de morte tem limites, deduplicação e limpeza em OFF/troca de mundo/perfil. Tags respeitam orçamento e deixam o render nativo fora dele. Prévia de perfil é calculada nas operações da tela, sem reler arquivos a cada frame. Os controles novos também têm verificação gráfica de limites e sobreposição em 427×240, a escala automática comum de uma janela 1280×720.

Não foram importados código, logo, áudio, vídeo, modelos ou texturas Purxiy. O atlas do menu vem da instalação do próprio Minecraft; nenhum asset Mojang foi extraído para redistribuição. O tecido CC0 e seu crédito anterior continuam no pacote. As implementações novas não dependem de serviços online.

Veja [resultados e comandos](INTERACTIVE-STUDIO-VALIDATION.md), [capturas atuais](../captures/README.md) e [vídeo atual](../captures/video-atual/README.md) após a validação.
