# Alien Client — auditoria e primeiro incremento open source

Data da análise: 22/10/2026. Alvo: **Minecraft Forge 1.8.9, Java 8, LWJGL 2**. Base: `0.3.0-alpha.1` (commit `fa7fb2664fa0`).

## Matriz de compatibilidade e licenças

| Projeto e fonte inspecionada | Licença e condições | Integração Forge 1.8.9 | Decisão/impacto no Alien |
| --- | --- | --- | --- |
| [PanelStudio](https://github.com/lukflug/PanelStudio) ([README](https://github.com/lukflug/PanelStudio/blob/main/README.md), [LICENSE](https://github.com/lukflug/PanelStudio/blob/main/LICENSE), [exemplo Forge](https://github.com/lukflug/PanelStudio/tree/main/example-mod8-forge)) | **MIT**; manter aviso e copyright | **Sim**, `panelstudio` + `panelstudio-mc8-forge`, Java 8; o exemplo usa ForgeGradle 2.1 e mapeamentos `stable_22` | **Prioridade**. Reutilizar partes comprovadas sem derrubar o fluxo atual. A biblioteca completa exige integração de configurações, temas e eventos; testar custo gráfico antes de migrar HUD. |
| [OneConfig v1](https://github.com/Polyfrost/OneConfig) | **LGPLv3 e termos adicionais próprios**, conferir textos antes de distribuição | README atual cita **Ornithe** para 1.8.9, não Forge; versões históricas exigem estudo específico | Não adotar uma segunda GUI simultânea. Usar somente como referência de experiência de perfis/HUD. |
| [Patcher](https://github.com/Sk1erLLC/Patcher) ([LICENSE.md](https://github.com/Sk1erLLC/Patcher/blob/master/LICENSE.md)) | **CC BY-NC-SA 4.0** no repositório; atribuição, não comercial e compartilhamento pela mesma licença | Mod do Minecraft legado com correções e otimizações; detalhes dependem de patch/mixin | Estudar técnicas e benchmark; **não copiar código** ou incluir builds sem avaliação jurídica explícita. |
| [Nuit](https://github.com/FlashyReese/nuit) | **MIT**, conforme README e LICENSE | **Não diretamente**; versão atual para Fabric/NeoForge modernos (26.3) | Referência para formato de skybox, keyframes, névoa e transições. Port Java 8/GL legado seria projeto separado. |
| [CapeAll](https://github.com/Nonopichy/CapeAll) | **Inconsistência**: README informa LGPL 2.1; arquivo LICENSE da branch principal contém **Apache 2.0**; verificar a branch real antes de copiar | README declara Forge 1.8.9; implementação está em branch própria | Não incorporar por enquanto. Estudar preview/capas; evitar transmitir estados por canais não previstos sem autorização do servidor. |
| [OneLauncher](https://github.com/Polyfrost/OneLauncher) | **GPLv3** | Não é biblioteca Forge; launcher separado em Rust | Referência futura para instalação, contas e empacotamento Windows. Não incorporar código no núcleo Forge. |
| [Poly Haven](https://polyhaven.com/license) | **CC0** para seus HDRIs/texturas/modelos; observar termos do site e licença individual do recurso | Assets devem ser convertidos/otimizados (ex.: PNG equiretangular com limite do importador) | Futuro pipeline offline de recursos; **nenhum asset importado nesta revisão**. |

As licenças descritas são as encontradas nos arquivos dos respectivos repositórios/website nesta data; não equivalem a parecer jurídico nem autorizam copiar arquivos de terceiros de outros caminhos ou branches.

## O que existe hoje no Alien

A versão `0.3.0-alpha.1` contém `Ui.Button`, `PanelMenuScreen`, `AlienMenuScreen`, `HudEditorScreen`, `CosmeticStudioScreen`, `WorldStudioScreen` e persistência. A separação Legit/Full e seus testes já existem. O ClickGUI utiliza fonte Minecraft, componentes `GuiButton` e rolagem/colunas próprias. Há perfis e hooks Forge estáveis; substituir toda a UI simultaneamente aumentaria o risco de regressão.

## Incremento implementado nesta branch

- Reutilização **literal e atribuída** das classes independentes `Animation.java` e `SettingsAnimation.java` do **PanelStudio MIT** na pasta `src/main/java/com/lukflug/panelstudio/base/`, com texto MIT em `docs/licenses/PANELSTUDIO-MIT.txt`.
- `Ui.Button` passa a usar a animação por **tempo decorrido**, com easing do PanelStudio. A configuração existente `animationSpeed` ajusta a duração; `animations=false` dá feedback imediato. O comportamento de clique, seleção e módulos não muda.
- Botões recebem contorno e preenchimento de cantos suavizados por retângulos pequenos (sem shader novo, textura externa ou dependência remota). Texto é truncado no limite do botão.
- Testes JUnit em `PanelStudioAnimationTest` verificam transição, retorno e duração zero.

**Não é** a integração do tema GameSense completo; os arquivos de GUI `PanelStudio-MC8-Forge`, `CSGOLayout` e `GameSenseTheme` não entraram no build. Nenhum sistema de HUD, céu, cosmético ou Full foi removido/substituído nesta etapa.

## Validação e limites

- **Realizado:** um pequeno teste independente dos cálculos de `Animation` / `SettingsAnimation`, compilado com `javac --release 8` e executado em Java 21, passou (easing adiante/atrás e tempo zero).
- **Pendente:** build completo `./gradlew -Pedition=legit build` e `-Pedition=full build`; validação de bytecode/JAR e execução gráfica do Forge 1.8.9. Este ambiente não dispõe das dependências ForgeGradle/Minecraft e não acessa seus repositórios externos para baixá-las.
- **Não apresentar:** captura de tela como se fosse posterior à alteração; screenshots existentes na pasta `captures/` são da base anterior. Sem afirmação de FPS ou compatibilidade OptiFine.

### Próximo incremento verificável

1. Em ambiente Forge preparado, compilar Legit e Full e executar os smoke tests existentes em ambos.
2. Revisar visualmente `Ui.Button` em escala 1–4 e resoluções 4:3/16:9; verificar truncamentos e interação; comparar antes/depois por capturas novas.
3. Avaliar a dependência **completa** `panelstudio:0.2.4` + `panelstudio-mc8-forge` (licença, shading/relocação e performance) num menu alternativo, mantendo fallback `Panels/Studio`.
4. Migrar um painel por vez para widgets/toggle/dropdown e adaptadores `ModuleDescriptor`; nunca alterar estado de gameplay automaticamente.
5. Só depois experimentar renderização de texto própria, novas skyboxes e pipeline de cosméticos.

A branch demonstra uma primeira reutilização real de código MIT; **não valida a integração completa** do PanelStudio no launcher/Forge.
