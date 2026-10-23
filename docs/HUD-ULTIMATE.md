# HUD Studio Ultimate e Full no Unified

Branch `feature/hud-studio-ultimate-20261023`, baseada na main `933f93ef3498f204ee3858c78785fd4b7992d937` (redesign 0.4 + Performance Engine 2.0 + proteção de sessão multiplayer). Versão experimental `0.4.0-alpha.3`; sem merge na main.

## Auditoria anterior às alterações

Foram lidos HudConfig, HudManager, HudEditorScreen, HudModules, HudLayouts, ClientConfig, ModuleManager, CombatConfig/Manager e CombatSessionGuard antes das modificações. Existiam 12 widgets (brand, módulos, FPS, CPS, teclas, coordenadas, ping, relógio, inventário, armadura, performance e texto), 9 switches visuais e 2 de combate na Unified. O editor já tinha drag/resize, alinhamento, estilos e intercâmbio limitado ao HUD. Não havia Target HUD, crosshair customizado, presets PvP/Survival/Cinematic ou os módulos Full solicitados.

Os 12 widgets e seus IDs/configurações foram mantidos. São adicionados `target`, `crosshair` e `indicators`, desativados em perfis antigos até serem escolhidos. Os módulos Full são recursos da Unified; as composições históricas Legit/Full não se tornam instalações paralelas necessárias.

## HUD implementado

- **Target HUD:** entidade sob a mira ou atacada; avatar da skin real para jogadores, nome, HP real e barra animada com interpolação independente do FPS. Para mobs, inicial do nome. Retenção/range, suavização e avatar configuráveis. Troca de mundo, remoção/morte e expiração removem o alvo. A prévia do editor usa o jogador carregado e é identificada como tal; sem alvo no mundo, o painel fica oculto.
- **Crosshair:** Plus/T/Circle/Dot, ponto, contorno, espessura, gap, comprimento, cor/opacidade e expansão conforme movimento real. Substitui a mira vanilla apenas quando habilitado em primeira pessoa. Pode ficar centralizado ou ser arrastado pelo editor.
- **Keystrokes:** bindings reais de movimento, pulo, LMB/RMB e estado de sprint, com transições suavizadas. CPS usa eventos reais de entrada de mouse numa janela de um segundo, com memória limitada; não inventa cliques a partir de ataques automáticos.
- **Armor:** ícones do inventário real, durabilidade numérica ou percentual e barras por peça, com destaque de baixa durabilidade.
- **Indicadores:** HP, velocidade horizontal em blocos/s, direção e até quatro efeitos ativos com amplificador/duração. Opções independentes.
- **Editor:** preserva drag, alça de resize, roda, snap, alinhamento, grade, export/import. Options abre os controles do widget selecionado e seu estilo/escala/cor/opacidade. HUD presets abre PvP, Survival, Cinematic e o Compact original, além de Inherit/Midnight/Cyan/Light. Os presets só mudam o HUD.

A normalização ocorre ao carregar/salvar, não em cada chamada de desenho. Lista de módulos é calculada no máximo a cada 100 ms; relógio a cada segundo. Caches de animação são pequenos e limpos por mundo. Ícones vanilla de itens mantêm seu renderizador original; a opacidade customiza os painéis/textos, não promete composição uniforme dos ícones.

## Módulos Full implementados

No Combat Studio, abra **Full modules**, ou abra um cartão da categoria Combat. O master e a autorização da sessão multiplayer continuam valendo. Novos perfis começam OFF. LEGIT desliga os extras; FULL habilita Aimbot, Auto Food, Auto Potion e Auto Sprint. Bridge e Anti Aim exigem ativação individual. Módulos têm keybinds/favoritos no mesmo registro dos demais.

| Módulo | Comportamento real | Limites |
| --- | --- | --- |
| Aimbot | Alinha instantaneamente a câmera ao jogador visível selecionado, usando FOV/range configurados. | Não é silent aim; não aumenta alcance de ataque ou hitboxes. |
| Auto Food | Seleciona comida da hotbar abaixo do limite de fome, usa o item vanilla e restaura o slot. | Não procura no inventário inteiro; evita alimentos venenosos comuns. |
| Auto Potion | Usa poções de cura/regeneração da hotbar abaixo do limite de HP; suporta beber e splash aos pés. Restaura câmera/slot. | Sujeito à disponibilidade real da poção e regras do jogo. |
| Anti Aim / spin | Gira a câmera real quando habilitado. Desativa os controladores de mira concorrentes. | Não faz fake angles/desync e não impede inimigos de acertarem; o nome não promete essa proteção. |
| Auto Bridge | Coloca blocos da hotbar abaixo dos pés quando há suporte vizinho e alcance válido. | Não é pathfinding até um destino, não cria blocos grátis e não constrói sobre vazio sem suporte. |
| Auto Sprint | Ativa sprint ao andar para frente com fome suficiente, sem colisão/sneak. | Regras vanilla permanecem. |

Automação libera o uso de item ao abrir GUI, desligar master, trocar perfil/mundo ou morrer; restaura apenas o slot/câmera do jogador sobre os quais assumiu controle. Nenhum fluxo de perfis foi removido e a aprovação de servidor continua temporária por mundo/perfil.

## Referências pesquisadas

O catálogo público [Wurst7 / hacks](https://github.com/Wurst-Imperium/Wurst7/tree/master/src/main/java/net/wurstclient/hacks) lista AutoEat, AutoPotion, AutoSprint, BowAimbot e ScaffoldWalk. Os nomes e categorias serviram como comparação de funcionalidades comuns; Wurst7 usa outra versão/plataforma e licença GPL-3.0. Não foi copiado código, modelo ou textura dele. Os três endpoints do Wurst Wiki consultados responderam HTTP 403, portanto não são citados como documentação lida com sucesso. As implementações desta branch foram escritas para os controles reais do Forge 1.8.9 existente.

## Validação

Resultados, capturas reais, comandos e limitações: [HUD-ULTIMATE-VALIDATION.md](HUD-ULTIMATE-VALIDATION.md). A galeria atual é substituída por capturas da revisão validada, mantendo o histórico apenas nas revisões do Git.
