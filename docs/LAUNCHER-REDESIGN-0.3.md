# Alien Launcher 0.3 — redesign e validação

**Atualização de 29/10:** o layout e o emblema deste redesign foram preservados no launcher **0.3.0-alpha.3**. [Download corrigido](../downloads/launcher/v0.3.0-alpha.3/README.md) · [Validação atual](AUDIT-FIXES-2026-10-29.md) · [Capturas novas](../captures/launcher-atual/README.md). Os números e o JAR descritos abaixo são o histórico do redesign 0.3.0-alpha.2.

A interface foi refeita no [PR #19](https://github.com/AttackAlien/Alien-Client/pull/19), na branch `feature/launcher-unified-alpha2-20261026`, sem merge na main. O código Java e o núcleo de instalação/perfis/autenticação não mudaram neste redesign.

[Download Windows x64](../downloads/launcher/v0.3.0-alpha.2/README.md) · [Capturas reais atuais](../captures/launcher-atual/README.md) · [CI final](https://github.com/AttackAlien/Alien-Client/actions/runs/40343053042).

## Emblema atualizado — 0.3.0-alpha.2

Apenas o símbolo Alien foi substituído por uma arte roxa facetada com transparência, criada por geração de imagem a pedido do usuário. O mesmo PNG incorporado é utilizado no painel e na lateral, sem alterar a composição aprovada. A imagem é carregada uma vez e ajustada sem distorção pelos limites de alfa. O original está em `launcher/AlienLauncher/Assets/alien-mark.png`; as fotos de evidência continuam sendo capturas reais do Windows.

## Interface implementada

- Janela sem moldura padrão, com controles próprios de minimizar, fechar e arrastar.
- Navegação lateral funcional para jogar, configurações, arquivos do perfil e diagnóstico.
- Painel principal com hierarquia de tipografia, emblema Alien, cenário real do client e botão de jogar destacado.
- Seletor de perfis e botões com pintura própria, estados de foco/hover e tema escuro. Sem os campos brancos e bordas padrão da interface anterior.
- Configurações separadas: RAM por mouse/teclado em passos de 512 MB, seletor/restauração de Java 8 e logout.
- Diagnóstico com o mesmo tema, conteúdo real e cópia para a área de transferência. Progresso/cancelamento aparecem durante operações, sem barra de progresso fictícia em repouso.

O cenário é um asset da captura Forge original `93-alpine-menu-night.png`, recortado pelo renderer e escurecido sob o texto. Isso não representa uma nova sessão do Minecraft. As quatro novas imagens de evidência vieram da janela do launcher realmente executada no desktop Windows, por `CopyFromScreen`.

## Verificação

Código: `69ac3f43f73e2511cfbf19dc04e9c05ac6f293ef`, [run 40343053042](https://github.com/AttackAlien/Alien-Client/actions/runs/40343053042).

| Verificação | Resultado |
| --- | --- |
| Núcleo .NET / Linux | 22 testes aprovados |
| Núcleo .NET / Windows Server 2022 | 22 testes aprovados |
| Janela WinForms real | 36 verificações aprovadas |
| Navegação e RAM | Configurações abertas pelo botão real; tecla direita ajustou 4096 → 4608 MB; 6144 MB foram salvos e recuperados após fechar |
| Instalação Windows | Minecraft 1.8.9, Java 8, Forge e JAR real verificados |

[Relatórios originais em texto normalizado para Git](launcher-results/2026-10-26/emblem-0.3-alpha.2/) · [Metadados/hashes das imagens](../captures/launcher-atual/manifest.json). As 36 verificações incluem limites de 17 controles visíveis na tela inicial, não 36 fluxos de usuário independentes.

As primeiras capturas reais revelaram duplicação de conteúdo durante a repintura de controles transparentes. Foram substituídos por superfícies opacas próprias, com limpeza explícita do buffer; os ícones dos botões compactos também foram centralizados. A captura final força a atualização real da janela antes de ler o desktop, evitando registrar a RAM antes da repintura. As fotos intermediárias não integram a galeria atual.

O pacote usa o mesmo Unified **0.6.0-alpha.2 / Forge 1.8.9**, JAR SHA-256 `17a1627a25b66423c1cf60bb745ca88b4888c5a376f93cb9cc736ae2e888c6fc`. O EXE é compilado para Windows x64 com .NET incluído. Os probes e caches de conta não são distribuídos.

Login Microsoft interativo/licença, sessão real/logout, gameplay em PC Windows físico, alto DPI e GPU física continuam sem validação. Esta é uma alpha. Não houve nova execução gráfica do Minecraft nesta alteração de interface; a execução anterior e seus limites estão no [relatório do núcleo 0.2](LAUNCHER-VALIDATION.md). Não foram repetidos JUnit porque o código Java e o JAR do client não mudaram.
