# Validação do redesign 0.4.0-alpha.1

Executada em 22/10/2026: Linux x86_64, Temurin 8u472, Gradle 2.14.1, ForgeGradle 2.1, Minecraft/Forge 1.8.9-11.15.1.2318 e Mesa llvmpipe. A [galeria atual](../captures/README.md) contém somente imagens desta revisão; as fotos antigas foram removidas da main a pedido do usuário.

| Resultado final | Legit | Full |
| --- | --- | --- |
| Clean build e inspeção do JAR | Passou, 98 classes Java 8 | Passou, 98 classes Java 8 |
| JUnit | 101 testes; zero falhas, erros ou ignorados | 101 testes; zero falhas, erros ou ignorados |
| Minecraft/Forge real | 124 verificações passaram | 124 verificações passaram |
| PNGs originais do framebuffer | 44 | 44 |

[Resultado original Legit](../captures/atual/legit/result.txt) · [Resultado original Full](../captures/atual/full/result.txt) · [SHA-256 dos binários](../downloads/v0.4.0-alpha.1/SHA256SUMS.txt).

## Evidência gráfica

Foram capturados menu inicial, ClickGUI, Cosmetic Studio, World Studio, HUD Studio, Theme Studio, configurações/perfis e a edição Full, além dos efeitos e widgets no mundo. As capturas incluem 854×480, 960×600 (16:10) e 800×600 (4:3), com menus em escala 0,85. Guardas de bounds confirmaram controles dentro das telas exercitadas.

A execução confirmou o renderizador Open Sans real, passes de blur sobre o framebuffer, canvas HSV visível nos pixels, botões de temas alterando tokens persistentes, editor de atalho escrevendo uma tecla real, menus responsivos e ausência de erro OpenGL nas verificações. A prévia 3D passou a exigir pixels de pele na região da cabeça, além das malhas dos acessórios, para detectar regressão de transparência do personagem.

As regressões anteriores permaneceram: edição do HUD, perfis, nove interruptores reais, atalho Right Shift, eventos de keybinding, capa em movimento, panorama PNG, céu em 2/4 chunks, terreno preservado e horário/clima reais do servidor intactos. Os GIFs usam 12 PNGs originais por edição, sem interpolação, a 8 quadros/s; os PNGs não receberam retoques.

## Problemas encontrados e corrigidos

- O corpo da prévia ficava invisível porque uma cor com alpha baixo do fundo era herdada pelo renderizador. O editor define cor branca opaca antes de desenhar o jogador; a validação agora verifica sua pele no framebuffer.
- O canvas de cores era ocultado pelo estado de profundidade/culling herdado do mundo. Os editores de cores isolam esses estados ao desenhar; um teste de pixels confirma o canvas visível.
- A lista do HUD podia sobrepor seus botões inferiores e os widgets apareciam sob a sidebar. O cálculo de linhas e o scissor do canvas foram corrigidos.
- Uma rodada travou ao encerrar o mundo: a ação vanilla 1.8.9 fechava a conexão antes de agendar o shutdown, permitindo que o servidor parasse antes da tarefa. Somente o harness foi alterado para usar `Minecraft.loadWorld(null)` mantendo a conexão até o agendamento normal. As rodadas finais descarregaram o mundo e encerraram o processo com status zero. A Full registrou uma exceção de pacote de chunk tardio após o descarregamento; isso não foi classificado como validação de rede/multiplayer sem erros.
- Builds incrementais antigos deixavam classes internas de telas removidas. Os pacotes finais foram recompilados com `clean build`, e o script agregado passou a limpar as saídas antes de empacotar.

## Comandos executados

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
./gradlew --no-daemon -Pedition=full clean build
env -u DISPLAY ALIEN_EDITION=full ./scripts/smoke-client.sh
./gradlew --no-daemon -Pedition=legit clean build
env -u DISPLAY ALIEN_EDITION=legit ./scripts/smoke-client.sh
```

O runner usa Xvfb/OpenGL por software e diretórios descartáveis `build/<edition>/smoke-runtime`. Os resultados atuais ficam em `build/<edition>/smoke/`; o mod de teste e classes Minecraft/Forge não entram nos JARs. O verificador inspecionou bytecode, recursos, identidade da edição, origem das classes e reofuscação SRG.

## Limites

Os testes gráficos executam classes atuais de desenvolvimento, separadas por edição. Os JARs foram compilados/reofuscados/inspecionados; instalação em launcher externo, Windows, GPU física, áudio, OptiFine, outros mods e servidores privados/públicos não foram validados. Consultas online de skin podem usar a aparência padrão; o ambiente não tem dispositivo de áudio. O limite de 30 FPS e os valores visíveis não são benchmark nem promessa de desempenho.

Os 124 checks incluem capturas e guardas de estado; não significam 124 módulos. A mesma suíte JUnit foi executada em ambas as composições. Os limites de recursos/UI estão em [UI-OVERHAUL.md](UI-OVERHAUL.md); módulos avançados Full continuam planejados. Evidências históricas permanecem nos tags anteriores, sem manter as imagens antigas na galeria atual.
