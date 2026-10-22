# Alien Client Legit/Full — 0.3.0-alpha.1

**Primeira fase do master plan: auditoria e separação de arquitetura.** A Full desta revisão ainda não implementa módulos avançados de combate, movimento, ESP ou automação. Preserva os recursos visuais compartilhados da base 0.2.0.

## Downloads

- **[AlienClient-Legit-1.8.9.jar](https://github.com/AttackAlien/Alien-Client/blob/v0.3.0-alpha.1/downloads/v0.3.0-alpha.1/AlienClient-Legit-1.8.9.jar?raw=true)**
- **[AlienClient-Full-1.8.9.jar](https://github.com/AttackAlien/Alien-Client/blob/v0.3.0-alpha.1/downloads/v0.3.0-alpha.1/AlienClient-Full-1.8.9.jar?raw=true)** — prévia de arquitetura.

[70 capturas reais e capas em movimento](https://github.com/AttackAlien/Alien-Client/blob/v0.3.0-alpha.1/captures/editions/README.md) · [SHA-256](https://github.com/AttackAlien/Alien-Client/blob/v0.3.0-alpha.1/downloads/v0.3.0-alpha.1/SHA256SUMS.txt) · [Código-fonte ZIP](https://github.com/AttackAlien/Alien-Client/archive/refs/tags/v0.3.0-alpha.1.zip)

Requer Minecraft Java 1.8.9, Forge 11.15.1.2318 e Java 8. Coloque **um único** JAR em `mods`, removendo a versão anterior. As duas composições têm o mesmo ID Forge; não as instale juntas. Right Shift abre o menu. Os links baixam os binários verificados versionados no repositório; não há anexos nativos em Assets, pois o serviço de uploads desta integração recusou a autenticação.

## Entregue nesta fase

- Auditoria da base e classificação dos requisitos/limitações do master plan.
- Fontes, providers, recursos, saídas de compilação e configurações separados por edição.
- Registro de nove interruptores visuais reais, metadados e rejeição de registros Full/não visuais na Legit.
- Atalhos individuais por perfil em Options → Controls → Alien Modules, sem ativação durante editores.
- Preservação dos cosméticos curvos, capa animada, HUD, céus/partículas, perfis e painéis da 0.2.0.
- Verificador do JAR reofuscado: bytecode, origem das classes, referências, provider, manifesto e identidade Forge.

## Validação executada

**Cada edição passou em 91 testes JUnit e 100 verificações no Minecraft/Forge real**, produzindo 35 capturas de framebuffer. O verificador rejeitou uma cópia de teste da Legit contendo o provider realmente compilado da Full. O resultado não representa implementação ou testes de módulos avançados inexistentes.

Os testes gráficos executaram classes atuais de desenvolvimento em Xvfb/OpenGL por software. Os JARs foram compilados/reofuscados/inspecionados, mas ainda não instalados em launcher externo. Sem teste de servidores privados/públicos, OptiFine, áudio, outros sistemas ou GPUs reais; sem benchmark comparativo.

[Arquitetura e próximos incrementos](https://github.com/AttackAlien/Alien-Client/blob/v0.3.0-alpha.1/docs/EDITIONS.md) · [Auditoria](https://github.com/AttackAlien/Alien-Client/blob/v0.3.0-alpha.1/docs/ARCHITECTURE-AUDIT.md) · [Relatório e comandos](https://github.com/AttackAlien/Alien-Client/blob/v0.3.0-alpha.1/docs/VALIDATION-EDITIONS.md).
