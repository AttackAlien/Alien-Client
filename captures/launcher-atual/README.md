# Alien Launcher 0.3.0-alpha.2 — novo emblema

**26/10/2026 às 11:50 (Bahia)**. Quatro capturas novas da janela realmente executada no Windows Server 2022, por `Graphics.CopyFromScreen`. Sem mockups, montagem ou geração por IA. O novo emblema aparece no painel e na lateral; o restante da interface foi preservado. As capturas anteriores do launcher foram removidas desta pasta; somente a rodada atual está publicada aqui.

[Baixar launcher 0.3](../../downloads/launcher/v0.3.0-alpha.2/README.md) · [Validação e limites](../../docs/LAUNCHER-REDESIGN-0.3.md) · [CI aprovado 40343053042](https://github.com/AttackAlien/Alien-Client/actions/runs/40343053042) · [Metadados e hashes](manifest.json).

## Tela inicial

![Launcher real — tela inicial nova](01-launcher-alien.png)

## Perfil Vanilla independente

![Launcher real — perfil Vanilla](02-launcher-vanilla.png)

## Configurações e controle de RAM

O teste abriu esta tela pelo botão real, ajustou a RAM pelo teclado e salvou 6 GB. O valor foi recuperado depois de fechar a janela.

![Launcher real — configurações](03-launcher-settings.png)

## Diagnóstico

Conteúdo real da sessão de teste, sem credenciais. O relógio dentro da janela usa UTC, o fuso do runner.

![Launcher real — diagnóstico](04-launcher-diagnostics.png)

**22 testes Linux, 22 Windows e 36 verificações da interface passaram.** A conta está desconectada; não foi simulado um login Microsoft. O cenário no painel principal é um asset de uma captura anterior do client. Estas imagens comprovam a interface nova em execução, sem representar nova execução do Minecraft ou gameplay Windows.
