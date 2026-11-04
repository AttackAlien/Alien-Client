# Integração e release 0.10.0-alpha.1

Os 16 PRs que estavam abertos (#1, #2, #6, #7, #12 e #15–#25) foram analisados e seus heads foram incorporados à branch de integração por commits de merge. A lista, os SHAs e a decisão sobre cada entrega estão em [integration.json](integration.json).

Os PRs #15–#25 já formavam uma cadeia de ancestrais da versão atual. Nos cinco PRs históricos foi usada a estratégia de merge `ours` após comparar suas entregas com a implementação atual: o código funcional já estava integrado ou havia sido substituído por uma implementação mais recente. Isso conserva o histórico sem regredir as interfaces, as correções da auditoria ou os testes. Os arquivos exclusivos do PR #2 foram preservados byte a byte em [downloads/archive/v0.1.0](../../../downloads/archive/v0.1.0/README.md), incluindo os hashes originais. As cinco fotos antigas do PR #1 continuam no histórico e não voltaram à galeria atual.

SkyboxGallery e CosmeticLooks mantêm os mesmos arquivos centrais do PR #7. Seus testes continuam presentes, com coberturas adicionais de independência dos looks. O empacotador do PR #6 ganhou verificação de proveniência e rejeição de evidência obsoleta; seus checks de ZIP no Windows continuam ativos. O launcher do PR #12 conserva login, descoberta de versões, instalação e instâncias Vanilla independentes, com a interface e as correções já validadas.

Os 223 testes JUnit passaram novamente na branch integrada, sem falhas ou testes ignorados. [XMLs originais](integration-junit-xml.zip).

Nenhum arquivo de execução do client ou launcher foi alterado nesta integração. Os objetos Git de src, scripts, build, launcher e workflows coincidem com as revisões aprovadas do client e launcher registradas no JSON. Os binários originais, capturas, GIFs e vídeo dessas revisões foram preservados. A main integrada recebe uma nova execução dos CIs; os resultados finais e os hashes dos arquivos publicados constam nas [notas e anexos do release](https://github.com/AttackAlien/Alien-Client/releases/tag/v0.10.0-alpha.1).

O release é **alpha**, para Minecraft Java 1.8.9 / Forge 11.15.1.2318 / Java 8. Login/licença Microsoft, multiplayer externo, GPU física e gameplay em Windows físico não foram validados. Os checks de interface/instalação do launcher foram executados no Windows Server 2022. [Validação funcional anterior, comandos e limites](../../ANIMATION-SKY-POLISH-VALIDATION.md).
