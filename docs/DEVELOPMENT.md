# Desenvolvimento

## Estrutura

- `AlienClient`: ciclo de vida do Forge, keybinding, troca opcional do menu e contexto de servidor.
- `config`: POJOs versionados, normalização, gravação atômica, backup, perfis e associações.
- `gui`: painel de navegação, componentes, temas e menu inicial.
- `hud`: renderizadores informativos e editor de layout proporcional à resolução.
- `cosmetic`: camadas de jogador, geometria original, cache/importação e Inspector por UUID.
- `world`: handlers de céu/clima, importação de panorama, partículas, neblina, câmera e filtro de cor.
- `performance`: orçamentos, presets e amostras reais de tempo entre frames.
- `src/test`: regressões JUnit que não precisam abrir o jogo.
- `src/smoke`: verificações gráficas separadas, quando disponíveis; não fazem parte do JAR distribuído.

Os renderizadores consultam a configuração atual; carregar um perfil não deixa referências antigas permanentes. Toda a execução dos módulos ocorre no cliente. O mod não registra pacotes, não altera velocidade/alcance/dano e não escreve horário/clima do mundo.

## Cadeia de build

As versões antigas são intencionais: ForgeGradle 2.1 e Gradle 2.14.1 correspondem a Forge 1.8.9. O wrapper moderno apenas inicia e verifica a distribuição fixada; ele não muda a versão do Gradle que executa o projeto. `build` compila Java 8, roda JUnit, empacota e reofusca o mod. As dependências do Minecraft e do Forge ficam fora do JAR produzido.

O ForgeGradle legado usa HTTP para recursos do jogo. O script de compatibilidade substitui o transporte por HTTPS para os mesmos arquivos oficiais e verifica o SHA-1 e tamanho definidos no manifesto original. A assinatura dos pacotes, TLS e verificações de integridade não são desativados.

Atrás de proxy, `scripts/gradle-env.sh` configura os parâmetros Java de proxy e um truststore local com as raízes do JDK e os certificados públicos providos pelo sistema. Não copia credenciais para os scripts. As ferramentas ficam em `.tools/` ou no diretório indicado por `ALIEN_TOOLS_DIR`; dependências, mundos de teste, recursos e logs são ignorados pelo Git.

## Ambiente na nuvem

Cada tarefa já tem isolamento. Use o checkout existente em `/workspace/Alien-Client`; não crie outro worktree por padrão. O snapshot retém ferramentas e caches, mas processos gráficos precisam iniciar novamente. Não há serviço backend obrigatório nem chave de API para usar o mod.

O build exige acesso aos repositórios oficiais do Forge, Gradle, Maven, Temurin e Minecraft. O bootstrap e os scripts de build descrevem os destinos reais. Publicar um rascunho de configuração é uma ação separada de compilar e testar nesta instância.

## Testes

```bash
./gradlew test
./gradlew build
```

As regressões cobrem persistência/importação de perfis, isolamento por servidor, limites de JSON, recuperação de dados inválidos, temas, valores de HUD/cosméticos/efeitos e decodificação de panoramas. Elas não substituem verificações OpenGL, conectividade com servidores ou um comparativo de desempenho.

Evite adicionar transformadores de bytecode como primeira opção. Prefira eventos públicos do Forge e restaure handlers/estado gráfico ao desativar um módulo. Recursos importados devem permanecer limitados, locais e sem execução de código. Novas configurações precisam de padrões e normalização para perfis anteriores.
