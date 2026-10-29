# Alien Launcher 0.3.0-alpha.3 — capturas atuais

**29/10/2026, 09:43 no fuso da Bahia (UTC−3)**. Seis PNGs originais da janela realmente executada no Windows Server 2022, por `Graphics.CopyFromScreen`. Capturas anteriores desta pasta foram substituídas. Layout e emblema aprovados foram preservados.

[Baixar launcher corrigido](../../downloads/launcher/v0.3.0-alpha.3/README.md) · [Resultados e limites](../../docs/AUDIT-FIXES-2026-10-29.md) · [CI 40875290339](https://github.com/AttackAlien/Alien-Client/actions/runs/40875290339) · [Metadados e hashes](manifest.json) · [Minecraft executado no Forge](../README.md).

## Tela inicial — Unified 0.6.0-alpha.3

![Janela real do launcher com o client corrigido](01-launcher-alien.png)

## Perfil Vanilla independente

![Vanilla 1.21.1 selecionado na janela real](02-launcher-vanilla.png)

## Configurações e RAM

![Configurações reais com 6 GB salvos](03-launcher-settings.png)

## Diagnóstico

![Diagnóstico real da sessão](04-launcher-diagnostics.png)

## Vanilla salvo restaurado ao reabrir

O teste salvou 1.21.4, reabriu a janela e confirmou o seletor, o botão Jogar Minecraft e a persistência após fechar. A versão não foi substituída por Alien.

![Vanilla 1.21.4 restaurado na janela real](05-launcher-restored-vanilla.png)

## Configuração inválida recuperada

A janela abriu após encontrar um JSON válido maior que 16 KiB, recuperou padrões e preservou o original em backup. O teste abriu o diagnóstico e conferiu o nome exato do arquivo preservado.

![Aviso real de recuperação da configuração](06-launcher-settings-recovery.png)

**31 testes Linux, 31 Windows e 46 verificações da interface passaram**, incluindo limites de controles e checagens de captura. A instalação isolada do Forge/Java também passou. Não houve login Microsoft nesta coleta. O cenário do painel é um asset de uma captura anterior do Minecraft; estas fotos registram o launcher WinForms. O emblema é arte de marca incorporada ao produto; os PNGs de evidência são leituras reais do desktop, sem retoques ou geração de imagem.
