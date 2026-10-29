# Alien Client Unified 0.6.0-alpha.3 — correções da auditoria

Minecraft Java **1.8.9 / Forge 11.15.1.2318 / Java 8**. Preserva Interactive Studio, HUD/Full, cosméticos, trilhas, partículas e Scene Studio; corrige a persistência dos perfis e o carregamento das miniaturas.

[Baixar ZIP](AlienClient-Unified-Primeiro-Teste.zip) · [Baixar JAR](AlienClient-Unified-1.8.9.jar) · [Instalação](LEIA-ME-INSTALACAO.txt) · [SHA-256](SHA256SUMS.txt) · [Recibo original](RESULTADO-DOS-TESTES.txt).

Fonte `269ecd7ade044db00ab06e410763f3ddc1d1ade6`, [CI 40873708575](https://github.com/AttackAlien/Alien-Client/actions/runs/40873708575). **181 JUnit, 526 verificações gráficas no Forge real e 12 testes de empacotamento**, além da extração e integridade do ZIP no Windows. Os checks gráficos incluem estados, handlers e capturas; não equivalem a 526 testes visuais independentes ou a um benchmark.

[Correções, comandos e limites](../../docs/AUDIT-FIXES-2026-10-29.md) · [154 capturas novas](https://github.com/AttackAlien/Alien-Client/blob/eb5f0201a0aae9a3966f6b473c1493e2ee691530/captures/README.md) · [Launcher Windows corrigido](../launcher/v0.3.0-alpha.3/README.md) · [PR #21](https://github.com/AttackAlien/Alien-Client/pull/21).

Instale somente um JAR Alien na pasta `mods`; remova o anterior e faça backup das configurações. A correção impede novas sobrescritas de perfil e não recupera valores já perdidos. Alpha sem merge na main. Gameplay em Windows físico, GPU física, OptiFine e multiplayer externo continuam sem validação.
