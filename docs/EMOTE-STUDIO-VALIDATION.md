# Validação — Emote Studio 0.8.0-alpha.1

Branch `feature/emote-studio-20261104`, [PR #23](https://github.com/AttackAlien/Alien-Client/pull/23), baseada no Trail Studio do PR #22 e nas correções da auditoria. **Sem merge na main.** [Uso e limites](EMOTE-STUDIO.md).

## Resultado

| Validação de 04/11/2026 | Resultado |
| --- | --- |
| Unified / Java 8 | 204 JUnit, zero falhas, erros ou ignorados |
| Seis sessões Forge do Unified | 680 checks: geral 161, céu 37, combate 24, HUD/Full/cosméticos/trilhas/emotes 245, Scene Studio 71, Interactive Studio 142 |
| Empacotamento do client | 12 testes, incluindo rejeição de evidência obsoleta, aprovados |
| Legit e Full | 204 JUnit e 155 checks gráficos por composição |
| Sky Studio / Legit e Full | 204 JUnit e 37 checks gráficos por composição |
| Núcleo do launcher | 31 testes Linux e 31 testes Windows |
| Janela WinForms real | 46 checks e seis capturas nativas |
| Instalação real no Windows CI | Forge 1.8.9, Java 8 e JAR 0.8.0-alpha.1 em instância isolada |
| Arquivos distribuídos | ZIPs com CRC completo, JAR idêntico ao validado e EXE PE/AMD64 |

Os checks incluem controles, estados do jogo e capturas. Não representam 680 golden images nem um benchmark. [Recibos, XMLs e metadados originais](emote-results/2026-11-04/).

## Emotes exercitados no jogo

A seleção usa B, movimento nativo do mouse X11 e soltura da tecla. A roda, Floss, Orange Justice e Take the L foram capturados dentro do jogo. Os contadores dos hooks do modelo confirmaram aplicação das poses ao jogador e à armadura sincronizada pelo servidor integrado. O teste confirmou que a dança não muda as coordenadas reais nem o yaw da câmera.

Os testes também exercitaram transição entre danças, parada suave, restauração da câmera, atribuição de slots pelos controles reais, salvamento/recarregamento do perfil Unified, ocultação visual de item e acesso ao último emote em layout compacto com rolagem nativa. Movimento e ataque nativos cancelaram a reprodução. Dano real pelo servidor integrado diminuiu a vida e encerrou a dança. Abrir menu, trocar o perfil e desligar emotes também encerraram ou impediram a reprodução.

Glass e Skeleton continuaram animando pelos renderizadores e camadas existentes. A prévia usa o jogador real, incluindo pele e cosméticos. Não há boneco ilustrativo usado como evidência.

A preparação do fixture limpa os inputs que os testes anteriores possuíam: Xorg sem gerenciador de janelas pode consumir uma soltura de tecla dentro de uma GUI sem atualizar o estado de KeyBinding. O teste de ataque também espera a soltura chegar ao jogo antes de abrir o próximo editor. Isso não ignora movimento ou ataque no produto. O modo `-PemoteOnly` é uma rodada local parcial separada; os recibos finais vêm da sessão completa, com os 245 checks.

O CI anterior aprovou os 245 checks de emotes e depois falhou no SceneSmoke: observava apenas onGround em um callback de renderização e podia perder a fase curta do salto. O fixture corrigido usa Space nativo, registra a altura por tick e mantém os checks de aterrissagem e shader. A sessão local Scene corrigida passou 71 checks. [Registro da tentativa anterior](emote-results/2026-11-04/earlier-ci-attempt.json).

## Origem dos pacotes e imagens

Client: fonte `8725dd2826050689506e404cf6f1b0e7a3647fa0`, [CI 41828928551](https://github.com/AttackAlien/Alien-Client/actions/runs/41828928551), SHA-256 do JAR `9d5a7caefc731aa69a3cbc2536e3b0ae827a84b789b9db00fecd7d29491d8a0c`.

Launcher: fonte `e080fbf144df1e1f1b40adbd854b795b7cde9ca9`, [CI 41830735294](https://github.com/AttackAlien/Alien-Client/actions/runs/41830735294), SHA-256 do ZIP `d3c13c13f9b4149d3c84c370af4eb1ae68b30395bf90382d20b110baf382d254` (70,758,597 bytes).

Os **251 PNGs atuais do jogo** foram copiados sem alterações do CI aprovado; um vem da janela X11 e os demais do framebuffer. Os seis PNGs do launcher vêm da janela WinForms executada no Windows Server 2022, com métodos e horários no [manifesto](../captures/launcher-atual/manifest.json). O layout e o emblema aprovados foram preservados.

As fotos anteriores saíram das pastas atuais. Relatórios históricos continuam vinculados às suas versões e commits. O GIF de emotes usa 48 frames originais, **15.49 segundos**, na ordem Floss → Orange Justice → Take the L, com intervalos registrados. Trilhas: vinte frames novos com intervalos registrados. Capa: doze frames novos, conversão a 100 ms. Não há frames gerados ou interpolados, nem nova gravação contínua longa.

O launcher fixa o JAR do CI aprovado do client. Seus commits de atualização/publicação usam `[skip ci]` para preservar os resultados Java sem repetir fontes idênticas. O launcher recebe execução manual completa do próprio workflow, no SHA registrado acima. Os [objetos Git validados](emote-results/2026-11-04/validated-source-trees.json) permitem comparar os arquivos de execução com os respectivos commits testados. O recibo no ZIP Windows pode usar CRLF; o JAR é idêntico byte a byte.

## Comandos locais utilizados

```bash
ALIEN_TOOLS_DIR=/workspace/.alien-tools GRADLE_USER_HOME=$PWD/.gradle-user \
  ./gradlew --offline --no-daemon -Pedition=unified test

Xorg :113 -config "$PWD/scripts/ci-combat-xorg.conf" -noreset \
  -nolisten tcp -ac -logfile "$PWD/build/emote-local-display/xorg.log"

DISPLAY=:113 LIBGL_ALWAYS_SOFTWARE=1 ALIEN_TOOLS_DIR=/workspace/.alien-tools \
GRADLE_USER_HOME=$PWD/.gradle-user \
  ./gradlew --offline --no-daemon -Pedition=unified test runUltimateSmokeClient
```

O CI usa os seis runners `scripts/ci-*-smoke-xorg.sh` e empacota somente após os resultados aprovados. Launcher: `dotnet test launcher/AlienLauncher.Tests/AlienLauncher.Tests.csproj -c Release` e publicação `win-x64` independente; os comandos completos estão nos workflows versionados.

## Limites observados

Minecraft 1.8.9 / Forge 11.15.1.2318 / Java 8u472, Linux Xorg dummy e OpenGL Mesa por software. A execução usa identidade de desenvolvimento no mundo de teste; não comprova login Microsoft. GPU física, gameplay em PC Windows pessoal, conta licenciada/renovação/logout Microsoft, OptiFine, servidores externos e renderizadores de mods externos não foram validados.

Os emotes são adaptações procedurais próprias para braços/pernas rígidos, sem cotovelos, joelhos, dedos ou assets extraídos do Fortnite. **Não há música nem sincronização pela rede.** Outros jogadores não veem a dança somente por você instalar o client.

O runner não tem áudio funcional. Os logs ainda registram tarefas de chunks após descarregar mundos nos fixtures General/Scene: 81 e 78 ocorrências dessa pilha nesta rodada. Não são contagens de quedas do jogo. Todas as seis sessões concluíram com PASS. [Ocorrências selecionadas](../captures/atual/unified/known-log-errors.txt); logs completos no artifact do CI.

[Download do client](../downloads/v0.8.0-alpha.1/README.md) · [Launcher Windows](../downloads/launcher/v0.3.0-alpha.5/README.md) · [Fotos e GIF reais](../captures/README.md).

## Acesso público verificado

O ZIP do launcher, o JAR, amostras das fotos e o GIF foram baixados novamente sem autenticação a partir do commit `fb7f1d804b5d66c0bb5843ebd174c2818dbb1633`: oito URLs responderam HTTP 200 e seus SHA-256 coincidiram com os arquivos validados. [Recibo da conferência pública](emote-results/2026-11-04/public-download-check.json).
