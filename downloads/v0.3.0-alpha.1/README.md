# Legit/Full — 0.3.0-alpha.1, fase 1

| Arquivo | Composição |
| --- | --- |
| [Baixar AlienClient-Legit-1.8.9.jar](AlienClient-Legit-1.8.9.jar?raw=true) | Base visual, cosméticos, céu, HUD, perfis e atalhos |
| [Baixar AlienClient-Full-1.8.9.jar](AlienClient-Full-1.8.9.jar?raw=true) | Prévia de arquitetura Full com os recursos visuais compartilhados |

**A Full ainda não implementa os módulos avançados do master plan.** Esta é a entrega da primeira fase, de auditoria e separação. [Arquitetura e pendências](../../docs/EDITIONS.md) · [Auditoria](../../docs/ARCHITECTURE-AUDIT.md).

Use Minecraft Java 1.8.9, Forge 11.15.1.2318 e Java 8. Escolha **um** JAR e coloque em `mods`, removendo a versão Alien anterior. Os dois têm o mesmo ID Forge e não devem ser carregados juntos. Right Shift abre o menu.

Ambas as composições passaram em 91 testes JUnit e 100 verificações gráficas em Minecraft/Forge real. [70 imagens reais](../../captures/editions/README.md) · [Relatório e comandos](../../docs/VALIDATION-EDITIONS.md) · [SHA-256](SHA256SUMS.txt).

Os JARs foram reofuscados e inspecionados; a instalação em launcher externo e servidores privados/públicos ainda não foi validada. Sem benchmark de FPS, OptiFine ou GPU real.
