# Correções da auditoria — 29/10/2026

Os cinco achados prioritários e as três observações menores da [auditoria de 26/10](AUDIT-2026-10-26.md) receberam correções e regressões no [PR #21](https://github.com/AttackAlien/Alien-Client/pull/21), branch `fix/audit-findings-20261026`, baseada no PR #20. O layout e o emblema aprovados foram preservados. Sem merge na main.

**Resultado: os oito achados receberam correções e suas regressões passaram.** A classificação permanece alpha pelos limites de autenticação e hardware descritos abaixo. [Launcher 0.3.0-alpha.3](../downloads/launcher/v0.3.0-alpha.3/README.md) · [Unified 0.6.0-alpha.3](../downloads/v0.6.0-alpha.3/README.md) · [Fotos do launcher](../captures/launcher-atual/README.md) · [Fotos do Minecraft](../captures/README.md).

| Achado | Comportamento corrigido | Regressão |
| --- | --- | --- |
| A1 — perda de perfil | Selecionar B durante uma conexão vinculada a A não grava em A, B ou na configuração global. Edições seguintes usam B durante essa conexão; a associação do servidor continua em A. Desconectar restaura a base global. | Dois JUnit: preservação dos arquivos/associação, salvamento posterior, reconexão e rejeição de perfil inválido sem alterar o destino. O reprodutor original retorna `UNRELATED_PROFILE_OVERWRITTEN=false`. |
| A2 — configuração grande impede abertura | Leitura limitada a 16 KiB; configuração rejeitada preservada em `settings.invalid-*.json`, padrões recuperados e aviso na janela. Salvamento acima do limite não substitui o arquivo válido anterior. | Três testes .NET cobrem backup exato, próxima abertura e serialização excessiva. Runner Windows exercita a janela real com arquivo grande. |
| A3 — Vanilla salvo muda para Alien | A versão Vanilla válida salva entra na lista antes da restauração, mesmo fora das cinco opções iniciais. | Runner Windows abre e fecha a janela com Vanilla 1.21.1 e 1.21.4, verificando seletor, botão Jogar e JSON/RAM persistidos. |
| A4 — miniatura decodificada repetidamente | Formato e dimensões PNG são consultados antes dos pixels; imagem decodificada limitada a 256 px; cache de 64 revisões rejeitadas e até 16 texturas. Captura nova invalida a rejeição; fechar libera texturas/cache. | Quatro JUnit cobrem redução, dimensões excessivas, cache de rejeição e atualização do arquivo. Reprodutor do método real também executado com heaps de 64/256 MiB. |
| A5 — PRs encadeados sem CI Java | Workflows Forge, Sky e Unified aceitam PRs com qualquer base para caminhos relevantes, com checkout do SHA revisado. | O PR #21, baseado na branch da auditoria, disparou os três workflows automaticamente. |
| O1 — Java com erro aceito | Saída não zero de `java -version` é rejeitada antes de interpretar o texto. | Teste .NET e wrapper de teste com cabeçalho Java 8/saída 42. |
| O2 — versão com quebra de linha | Validação integral por `\A...\z`, apenas dígitos ASCII e pontos. | Casos .NET para LF, CRLF, espaço inicial e valor nulo. |
| O3 — versão inconsistente do launcher | `launcher/Directory.Build.props` centraliza a versão; interface, log e comando de execução usam a mesma informação do assembly Core. | Compilação do núcleo, UI e runner; diagnóstico real incluído na suíte Windows. |

O recibo do JAR agora distingue o commit revisado do merge sintético usado pelo evento de PR. O workflow confere o checkout antes da compilação e passa esse SHA ao empacotador; uma regressão verifica o conteúdo do recibo quando os dois SHAs diferem.

## Resultados locais

| Verificação | Resultado |
| --- | --- |
| Unified, Java 8 / Gradle offline | 181 JUnit, zero falhas, erros ou ignorados |
| Núcleo do launcher / .NET 8 Linux | 31 testes, zero falhas ou ignorados |
| Compilação da UI Windows e runner | Zero avisos e erros; compilação cruzada não equivale a execução gráfica |
| Probes originais da auditoria | Perfil A preservado; arquivo grande recuperado; Java com saída 42 e versão com LF rejeitados |

[Logs (espaços finais normalizados), TRX, XMLs e probes locais](audit-fix-results/2026-10-29/). A imagem sintética de 6000×6000 dos probes é entrada de teste, não captura de jogo. Tempos desse probe headless não são benchmark de FPS.

## CI, pacote e capturas reais

| Verificação | Resultado e origem |
| --- | --- |
| Unified / Java 8 | **181 JUnit**, zero falhas, erros ou ignorados; [CI 40873708575](https://github.com/AttackAlien/Alien-Client/actions/runs/40873708575), fonte `269ecd7ade044db00ab06e410763f3ddc1d1ade6` |
| Seis sessões de Minecraft Forge 1.8.9 | **526 checks**: geral 161, céu 37, combate 24, HUD/Full/cosméticos 92, Scene Studio 70, Interactive Studio 142 |
| Empacotamento do client | **12 testes Python**, incluindo rejeição de evidência antiga e recibo com SHA correto; extração/integração do ZIP também aprovada no Windows |
| Regressão Legit / Full | **181 JUnit e 155 checks gráficos por edição**; [CI 40873708647](https://github.com/AttackAlien/Alien-Client/actions/runs/40873708647) |
| Sky Studio / Legit e Full | **181 JUnit e 37 checks gráficos por edição**; [CI 40873708629](https://github.com/AttackAlien/Alien-Client/actions/runs/40873708629) |
| Launcher / Linux e Windows | **31 testes .NET por sistema**, zero falhas ou ignorados; [CI final 40875290339](https://github.com/AttackAlien/Alien-Client/actions/runs/40875290339), fonte `943ec678deb089bc7748b8df3ff9f4d891d62c89` |
| Janela Windows real | **46 checks e seis PNGs originais**, incluindo restauração 1.21.4, recuperação do arquivo grande e diagnóstico com o nome exato do backup |
| Instalação Windows | `INSTALL_PASS`: instância isolada Minecraft 1.8.9 / Forge 11.15.1.2318 / Java 8 e JAR corrigido verificados, sem autenticação de teste |
| ZIP do launcher | CRC completo, EXE PE/AMD64, hash do JAR, manifesto de origem e ausência de probes/caches de conta conferidos; **70.712.496 bytes**, copiado sem alteração do CI |

O JAR distribuído e incluído no launcher tem SHA-256 `fe39c46a8b91a22cae7503d2dd1bfabfdf7ab98e8260d51c2643bffe97ae7842`. O ZIP Windows tem SHA-256 `e680a086c39474e0f13c72b4c5ada032837c54726e0fe62ee4066f7870ac6f7f`. O launcher fixa a origem no CI bem-sucedido do client; não recompila ou troca silenciosamente seu JAR. O recibo textual dentro do ZIP Windows mantém o mesmo conteúdo, com terminações de linha convertidas para CRLF pelo checkout Windows; o JAR é idêntico byte a byte.

[Resumo do client](audit-fix-results/2026-10-29/client-summary.json) · [Resumo do launcher/pacote](audit-fix-results/2026-10-29/launcher-summary.json) · [Regressão das edições](audit-fix-results/2026-10-29/edition-ci-summary.json) · [UI original](audit-fix-results/2026-10-29/launcher-ui-result.txt) · [Instalação original](audit-fix-results/2026-10-29/launcher-windows-install.txt).

Após fixar o novo payload, os cinco workflows também concluíram com sucesso no SHA `943ec67`: Unified, Forge Legit/Full, Sky Studio, launcher e coleta Windows da auditoria. [Execuções da revalidação](audit-fix-results/2026-10-29/revalidation-after-pin.json). ZIP, JAR e amostras dos PNGs publicados foram baixados sem cabeçalho de autenticação, retornaram HTTP 200 e mantiveram os hashes verificados; [registro público](audit-fix-results/2026-10-29/public-download-check.json).

As imagens atuais do jogo foram copiadas sem alteração do CI do client: **154 PNGs novos**, incluindo 153 capturas do framebuffer e uma da janela X11. O GIF da capa foi refeito com os doze frames novos. A galeria do launcher contém apenas os seis PNGs da execução Windows final. Os arquivos de imagem anteriores e o vídeo da alpha.2 foram retirados das pastas atuais; seu histórico continua no Git. Não foi gravado um novo vídeo contínuo nesta correção.

O código Java preserva o mesmo estado validado pelo CI `40873708575`; a revisão seguinte atualiza o payload fixado e o aviso de recuperação do launcher. Os commits de publicação das evidências e downloads não alteram código de runtime, testes ou workflows. Os manifests identificam separadamente a fonte do client e a fonte do launcher. [Objetos Git dos inputs de runtime validados](audit-fix-results/2026-10-29/validated-source-trees.json). O commit de publicação usa `[skip ci]` porque altera somente documentação, downloads e capturas; a compilação e os testes são os dos SHAs identificados acima.

## Reprodução

```bash
ALIEN_TOOLS_DIR=/workspace/.alien-tools GRADLE_USER_HOME="$PWD/.gradle-user" \
  ./gradlew --offline --no-daemon -Pedition=unified test
ALIEN_TEST_PAYLOAD="$PWD/launcher/payload" ALIEN_TEST_JAVA8="$JAVA_HOME/bin/java" \
  dotnet test launcher/AlienLauncher.Tests/AlienLauncher.Tests.csproj \
  --disable-build-servers -m:1 -p:UseSharedCompilation=false
python3 scripts/audit-local-probes.py --java-home "$JAVA_HOME" --output build/audit-local
dotnet run --project tools/audit/LauncherProbe/LauncherProbe.csproj
```

Os probes escrevem somente em diretórios temporários. O CI executou o jogo nestas sessões, com display Xorg dummy conectado e OpenGL Mesa:

```bash
bash scripts/bootstrap.sh
./gradlew --no-daemon -Pedition=unified clean setupDecompWorkspace build
export ALIEN_EDITION=unified
bash scripts/ci-smoke-xorg.sh
bash scripts/ci-sky-smoke-xorg.sh
python3 scripts/package-unified.py --begin-validation
bash scripts/ci-combat-smoke-xorg.sh
bash scripts/ci-ultimate-smoke-xorg.sh
bash scripts/ci-scene-smoke-xorg.sh
bash scripts/ci-studio-smoke-xorg.sh
python3 -m unittest discover -s scripts/tests -p 'test_package_unified.py' -v
python3 scripts/package-unified.py
```

Os runners chamam as tarefas `runSmokeClient`, `runSkySmokeClient`, `runCombatSmokeClient`, `runUltimateSmokeClient`, `runSceneSmokeClient` e `runStudioSmokeClient` do Gradle. São sessões reais com fixtures descartáveis e alvos locais; não comprovam multiplayer externo. O [workflow do launcher](../.github/workflows/alien-launcher.yml) registra os comandos exatos de publicação, execução nativa do runner WinForms e instalação Forge Windows.

## Limites

A correção de A1 impede novas sobrescritas; não reconstrói configurações já perdidas em instalações anteriores. Autenticação Microsoft com licença, renovação/logout reais, multiplayer externo, gameplay em PC Windows físico, alto DPI, GPU física e OptiFine continuam dependendo de validação real. Os testes automatizados não usam uma conta Microsoft fictícia para alegar esse funcionamento. O produto permanece alpha.

Os logs gráficos não estão livres de erros: as sessões General e Scene ainda registraram, respectivamente, **81 e 76 ocorrências da pilha de tarefas de chunks após descarregamento automático dos mundos**. Os checks seguintes e as seis sessões concluíram com PASS; esses números não são contagens de quedas do jogo. O runner também não possui áudio funcional. [Ocorrências e linhas selecionadas](../captures/atual/unified/known-log-errors.txt); os logs completos estão no artifact do CI. Estas limitações dos fixtures/ambiente não foram apresentadas como corrigidas.
