# Now Playing — resultados de validação

Rodada de 05/11/2026. Horários técnicos originais em UTC nos recibos; data de apresentação em America/Bahia. Candidato do [PR #29](https://github.com/AttackAlien/Alien-Client/pull/29), sobre o PR #28. Main e release público 0.10 preservados.

## O que passou

| Verificação | Resultado | Execução |
|---|---:|---|
| Unified / JUnit Java 8 | 268, zero falhas | [42051508518](https://github.com/AttackAlien/Alien-Client/actions/runs/42051508518) |
| Unified / Forge gráfico completo | 923 checks, incluindo 51 de mídia | mesmo CI |
| Empacotamento e rejeição de evidência desatualizada | 13 testes | mesmo CI |
| Legit e Full | 268 JUnit + 155 checks gráficos em cada edição | [42051508515](https://github.com/AttackAlien/Alien-Client/actions/runs/42051508515) |
| Céu nas duas edições anteriores | 268 JUnit + 37 checks gráficos em cada edição | [42051508429](https://github.com/AttackAlien/Alien-Client/actions/runs/42051508429) |
| Ponte nativa Windows 11 / SMTC | 9 checks com uma sessão real de diagnóstico | [42053831503](https://github.com/AttackAlien/Alien-Client/actions/runs/42053831503) |
| Launcher com o JAR 0.12 fixado | 37 testes Linux + 37 Windows | [42053831389](https://github.com/AttackAlien/Alien-Client/actions/runs/42053831389) |
| Janela real do launcher / instalação | 46 checks, seis capturas e instalação isolada Forge/Java 8 | mesmo CI |

Fonte do client: `28053d5fae099f7654d0ce47fea6d4b68e6566bb`. SHA-256 do JAR: `d59f41b116011cf52cb7607f281e8e65218029e054162844f12f11911d43e5bf`.
Fonte do launcher: `721fa8cd5bae384efc62a9b68d0c3957b1c45756`. SHA-256 do ZIP: `7f37a88e6d6eb31fe45709536ab0408322b35b6e9494ddb319167a4b88eb3336`.

Os testes Java verificam tempo pausado, velocidade e limites do progresso, busca/troca de faixa, linha do tempo desconhecida, expiração, Unicode, normalização, perfis antigos e novos, JSON inválido/profundo/grande, symlinks e checksum/dimensões das capas.

No Forge real: Compact/Card/Transparent, capa e reutilização/substituição/liberação da textura, editor, arrastar, escala e alinhamento, opções, opacidade e perfis, filtro de fonte, pausa, títulos longos e recorte, desconexão e limpeza dos arquivos temporários. Também foi verificada a restauração do estado OpenGL **e do cache do Minecraft**, para preservar os outros widgets. A primeira rodada revelou um buffer pequeno demais para `glGetInteger` no LWJGL 2; isso foi corrigido antes da rodada aprovada.

O teste nativo passou em **Windows 11 ARM64, build 26200**, no runner público `windows-11-arm`. Um publicador de diagnóstico criou uma sessão SMTC real. O executável do launcher, em modo de ponte, recebeu título/artista/capa com checksum e duração, manteve a posição pausada, limpou o estado ao parar e encerrou/removou arquivos quando o processo monitorado terminou. Os primeiros diagnósticos em Windows Server retornaram enumeração vazia; não foram contabilizados como aprovação da integração.

## Imagens e vídeo

A [galeria atual](../captures/README.md) contém **379 PNGs novos do jogo**, que substituem os anteriores, e seis novas capturas nativas do launcher. São arquivos originais da execução aprovada. [Manifesto e limites](../captures/atual/unified/validation.json) · [Hashes do jogo](../captures/atual/unified/SHA256SUMS.txt) · [Manifesto do launcher](../captures/launcher-atual/manifest.json).

[Equivalência das árvores de código](now-playing-results/2026-11-05/source-equivalence.json). A publicação final acrescenta somente mídia, downloads e documentação às fontes testadas.

As onze imagens de música foram renderizadas no Minecraft com uma **fonte sintética de metadados**, identificada no HUD. Elas comprovam a interface e o transporte local, sem representar uma sessão Spotify/YouTube. O teste Windows usa o publicador OS de diagnóstico separado, com [recibo](now-playing-results/2026-11-05/windows-media-result.txt) e [snapshot real recebido](now-playing-results/2026-11-05/windows-media-playing.json).

O [vídeo atual](../captures/video-atual/README.md) é uma gravação contínua de 26,07s da janela real do Forge, com a mesma fonte sintética. Sem áudio, aceleração ou interpolação. [Proveniência](now-playing-results/2026-11-05/video-provenance.json), SHA-256 `97221a09612941fc695d0a42c4e7c7312171d37281a0d0605684a65030ad3778`. A mídia anterior foi retirada das pastas atuais e permanece no histórico Git.

## Limites

- Spotify desktop e YouTube no navegador **ainda não foram testados com esses aplicativos reais**. O suporte depende de o aplicativo publicar metadados ao Windows. Navegadores são identificados pelo nome do aplicativo, sem adivinhar o site da aba.
- Não houve teste de gameplay em um Windows pessoal, GPU física, autenticação Microsoft interativa ou multiplayer externo. O launcher x64 foi testado nativamente em Windows Server; a ponte SMTC foi exercitada em Windows 11 ARM64. Não houve validação direta em Windows 10.
- A execução gráfica é Linux/Xorg/Mesa por software, com mundo integrado descartável. Estes checks não são um benchmark de hardware pessoal. Os logs originais incluem exceções de tarefas de chunks em trocas/encerramento dos mundos dos testes; as contagens foram preservadas em [known-log-errors.txt](../captures/atual/unified/known-log-errors.txt), sem tratá-las como contagem de crashes.
- A funcionalidade desta rodada é um HUD de leitura e personalização. Controles de reprodução, letras e análise do áudio não fazem parte da implementação.

## Reproduzir

```bash
ALIEN_TOOLS_DIR=/workspace/.alien-tools \
GRADLE_USER_HOME=/workspace/Alien-Client/.gradle-user \
./gradlew --offline --no-daemon -Pedition=unified test build

# Em Linux com Xorg, mesa-utils e dependências instaladas:
ALIEN_EDITION=unified bash scripts/ci-media-smoke-xorg.sh
```

O runner gráfico usa `runMediaSmokeClient`, um mod e publicador Python **exclusivamente de teste**, ausentes do JAR distribuído. Os controles são exercitados em um mundo temporário. O CI completo executa também os runners general/sky/combat/ultimate/scene/studio e os 13 testes de empacotamento.

```powershell
# Windows 10 1809+ ou Windows 11 com SDK .NET 8:
dotnet build launcher/AlienLauncher.MediaProbe -c Release -o media-probe
./media-probe/AlienLauncher.MediaProbe.exe media-results ./media-probe/AlienLauncher.exe
```

O probe cria somente uma sessão local de diagnóstico. Não requer conta Spotify. O fluxo normal do launcher continua exigindo uma conta Microsoft com Minecraft Java.

[Recibos originais](now-playing-results/2026-11-05/) · [Guia de uso](NOW-PLAYING.md) · [Client candidato](../downloads/v0.12.0-alpha.1/README.md) · [Launcher candidato](../downloads/launcher/v0.3.0-alpha.10/README.md).
