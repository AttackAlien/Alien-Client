# Alien Launcher 0.3.0-alpha.3 — Windows x64

**[Baixar ZIP — 67.44 MiB](https://github.com/AttackAlien/Alien-Client/raw/refs/heads/fix/audit-findings-20261026/downloads/launcher/v0.3.0-alpha.3/AlienLauncher-Windows-x64-0.3.0-alpha.3.zip)** · [Fotos novas](../../../captures/launcher-atual/README.md) · [Correções e testes](../../../docs/AUDIT-FIXES-2026-10-29.md) · [SHA-256](SHA256SUMS.txt) · [Recibo](RESULTADO-DOS-TESTES.txt).

Mantém o layout escuro e o emblema aprovado, recupera configurações inválidas e restaura a versão Vanilla salva. Inclui o **Unified 0.6.0-alpha.3 corrigido / Forge 1.8.9**, com integridade e origem fixadas.

1. Extraia todo o ZIP no Windows 10/11 x64 e abra `AlienLauncher.exe`. Mantenha a pasta `payload` ao lado do EXE; o .NET está incluído.
2. Escolha Alien ou Vanilla e use **Instalar / reparar** para baixar/verificar Minecraft, Java e Forge.
3. Use **Conectar conta** com uma conta Microsoft que possua Minecraft Java; requer WebView2 Runtime. Depois clique **Jogar Alien** ou **Jogar Minecraft**.
4. Ajuste RAM e Java em **Configurações**. **Diagnóstico** mostra registros reais e informa o nome do backup quando uma configuração foi recuperada.

Dados em `%LOCALAPPDATA%\AlienLauncher`. Atualizações preservam saves e mods de terceiros; versões anteriores do mod Alien são movidas para backup. O Alien usa Forge 1.8.9; perfis Vanilla modernos são independentes.

[CI aprovado 40875290339](https://github.com/AttackAlien/Alien-Client/actions/runs/40875290339): **31 testes Linux, 31 Windows, 46 checks da janela real, seis capturas e instalação isolada do Forge com Java 8**. Fonte do launcher `943ec678deb089bc7748b8df3ff9f4d891d62c89`. O ZIP foi copiado sem alteração do CI; CRC, EXE PE/AMD64 e hash do JAR conferidos. Não inclui os runners de teste nem caches de conta.

ZIP SHA-256: `e680a086c39474e0f13c72b4c5ada032837c54726e0fe62ee4066f7870ac6f7f`. JAR incluído SHA-256: `fe39c46a8b91a22cae7503d2dd1bfabfdf7ab98e8260d51c2643bffe97ae7842`; [origem do client](client-source.json).

**Alpha do PR #21, sem merge na main.** Login Microsoft interativo com licença, renovação/logout reais e gameplay em PC Windows físico continuam sem validação. O executável experimental não tem assinatura Authenticode.
