# Validação — Trail Studio 0.7.0-alpha.1

Branch `feature/trail-studio-20261029`, [PR #22](https://github.com/AttackAlien/Alien-Client/pull/22), baseada nas correções do PR #21. Sem merge na main. [Uso e limites do editor](TRAIL-STUDIO.md).

## Escopo implementado

Três pontos de cor/largura/transparência, arraste de curvas, entrada numérica, HSV/HEX, quatro texturas originais, PNG local, presets Nebula/Ember/Glacier, repetição por distância e rolagem da textura. Perfis e looks copiam o desenho sem compartilhar objetos mutáveis. O Designer começa desligado nos perfis antigos e a opção Particles foi preservada.

## Rodada local

**193 JUnit**, zero falhas, erros ou ignorados, e **152 verificações gráficas** na sessão HUD/Full/cosméticos/Trail Studio do Minecraft Forge 1.8.9. Java 8u472, Linux, Xorg dummy conectado e Mesa por software. [Console e XMLs](trail-results/2026-10-29/).

Os testes exercitaram handlers reais dos controles, mudanças nos pixels da prévia, digitação HEX, entrada numérica, persistência, seleção e renderização de um PNG local, três presets no mundo, 20 capturas durante movimento, layout compacto com rolagem nativa, fallback e OFF. O movimento lateral usa teclado X11; a câmera da sequência também recebe ajustes do fixture. Não é uma sessão manual em hardware físico.

```bash
ALIEN_TOOLS_DIR=/workspace/.alien-tools GRADLE_USER_HOME="$PWD/.gradle-user" \
  ./gradlew --offline --no-daemon -Pedition=unified test compileUltimateSmokeJava
mkdir -p build/trail-local-display
Xorg :112 -config "$PWD/scripts/ci-combat-xorg.conf" -noreset -nolisten tcp -ac \
  -logfile "$PWD/build/trail-local-display/xorg.log"
# Em outro terminal, após o display estar disponível:
DISPLAY=:112 LIBGL_ALWAYS_SOFTWARE=1 ALIEN_TOOLS_DIR=/workspace/.alien-tools \
  GRADLE_USER_HOME="$PWD/.gradle-user" \
  ./gradlew --offline --no-daemon -Pedition=unified runUltimateSmokeClient
```

## Ajuste da checagem de céus

O CI `40885490289` passou pelo Trail Studio, mas falhou numa checagem anterior do Scene Studio: BlackHole e Cosmic tinham RGB `(53, 38, 98)` no único pixel inspecionado. As oito regiões de céu capturadas tinham padrões diferentes. O fixture passou a comparar grades de pixels do céu, mantendo a exigência de oito resultados distintos; a nova execução local passou nas 70 verificações. [Diagnóstico com os dados originais](trail-results/2026-10-29/scene-failure-investigation.json). Essa correção está no teste, sem mudanças no renderizador de céus.

## CI e entrega

**Rodada completa aprovada.** [CI e origens](trail-results/2026-10-29/ci-runs.json) · [Client](../downloads/v0.7.0-alpha.1/README.md) · [Launcher Windows](../downloads/launcher/v0.3.0-alpha.4/README.md) · [Fotos do jogo](../captures/README.md) · [Fotos do launcher](../captures/launcher-atual/README.md).

| Verificação | Resultado |
| --- | --- |
| Unified / Java 8 | 193 JUnit, zero falhas, erros ou ignorados |
| Seis sessões Forge | 586 checks: geral 161, céu 37, combate 24, HUD/Full/cosméticos/Trail Studio 152, Scene Studio 70, Interactive Studio 142 |
| Empacotamento | 12 testes Python, incluindo rejeição de evidência antiga, e extração/integridade do ZIP no Windows |
| Legit / Full | 193 JUnit e 155 checks gráficos por composição |
| Sky Studio / Legit e Full | 193 JUnit e 37 checks gráficos por composição |
| Launcher Linux / Windows | 31 testes .NET por sistema, zero falhas ou ignorados |
| Janela Windows real | 46 checks e seis capturas nativas atuais |
| Instalação Windows | Forge 1.8.9 / Java 8 / JAR 0.7.0-alpha.1 aprovados em instância isolada |
| ZIP do launcher | CRC completo, EXE PE/AMD64, origem/hash do JAR e ausência de probes/caches de conta conferidos |

Client: fonte `f10f753eae78e95fa8a76112f5a58b689d5063ae`, [CI 40887787750](https://github.com/AttackAlien/Alien-Client/actions/runs/40887787750), JAR SHA-256 `69f5e881ab8f88e5a7bf36de44c3f217a83d4b343c94b369965f890a1cb0a240`. Launcher: fonte `a2f11fef4675af5fcf9260992a01a98767a5ed9f`, [CI 40890062885](https://github.com/AttackAlien/Alien-Client/actions/runs/40890062885), ZIP SHA-256 `9a54a20f6771cff56718c1f3be9f14407135201f3e89060a832630a04ea7d65a` (70,729,629 bytes).

[Resumo do client](trail-results/2026-10-29/client-summary.json) · [Edições](trail-results/2026-10-29/edition-ci-summary.json) · [Resumo do launcher](trail-results/2026-10-29/launcher-summary.json) · [UI original](trail-results/2026-10-29/launcher-ui-result.txt) · [Instalação original](trail-results/2026-10-29/launcher-windows-install.txt).

Os 190 PNGs do jogo foram copiados sem alteração do CI aprovado, assim como o ZIP Windows. São 189 capturas do framebuffer e uma da janela X11. Os seis PNGs do launcher vêm da janela WinForms realmente executada no Windows Server 2022. As pastas atuais substituem as imagens anteriores; evidências históricas continuam identificadas por versão e commit no Git. O GIF das trilhas contém 20 frames reais, 7,38 segundos, com intervalos de captura registrados; não é uma nova gravação contínua longa.

O launcher fixa o JAR do CI aprovado do client. Sua atualização e o commit de publicação usam `[skip ci]` para evitar repetir Java sem alterações; a atualização do launcher recebeu execução manual completa do próprio workflow no SHA acima. [Objetos Git dos inputs validados](trail-results/2026-10-29/validated-source-trees.json) permitem conferir que os commits de publicação alteram somente documentação, capturas e downloads. O recibo textual dentro do ZIP Windows tem o mesmo conteúdo com terminações CRLF; o JAR é idêntico byte a byte.

Os comandos completos da rodada remota estão em [unified-client-ci.yml](../.github/workflows/unified-client-ci.yml): build/JUnit, seis runners `scripts/ci-*-smoke-xorg.sh`, testes Python e empacotamento. A execução Windows está em [alien-launcher.yml](../.github/workflows/alien-launcher.yml).

## Limites

As imagens de teste são capturas do jogo, sem retoques ou geração. O PNG quadriculado usado no teste de importação é um recurso sintético deliberado, realmente renderizado no Minecraft; não é uma foto simulada do jogo. As máscaras prontas são arte procedural original do client.

O GIF foi montado com 20 frames reais e os intervalos registrados, sem interpolação. Não comprova sincronização remota, gameplay Windows, GPU física, OptiFine, multiplayer externo ou benchmark de FPS. Login Microsoft e hardware físico mantêm os limites da alpha anterior. O renderer mantém teste de profundidade, limites de geometria e caches limitados; não se promete ganho de FPS.

Os logs ainda registram limitações do runner: áudio indisponível e tarefas de chunks após descarregamento dos mundos nos fixtures General/Scene. Nesta rodada houve 81 e 74 ocorrências dessa pilha, respectivamente; não são contagens de quedas do jogo. As seis sessões e seus checks terminaram com PASS. [Ocorrências selecionadas](../captures/atual/unified/known-log-errors.txt); logs completos no artifact do CI.
