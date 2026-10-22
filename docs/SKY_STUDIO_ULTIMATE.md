# Sky Studio Ultimate — primeira implementação

**Branch:** `feature/sky-studio-ultimate` (criada da `main`, independente do PR #3).

## Mudanças concretas

- Importação de **cubemap 3×2 em um PNG** terminado em `*.cube.png` com seis faces quadradas (64..1024 px por face) e UVs separados com margem de meio texel.
- Panoramas PNG equiretangulares existentes **continuam suportados** com o mesmo código de projeção.
- Novos controles locais: yaw 0..360°, drift opcional 0..2°/s, brilho de imagem 20..100% e variação suave de luminosidade das estrelas.
- Editor **Sky options** acessível pelo World Studio, com botão Reset para apenas as novas opções.
- Validação do atlas **antes de decodificar a imagem**, mesmo limite de 16 MiB, diretório local e rejeição de caminhos externos.
- Testes JUnit de face/UV/raio, arquivos inválidos e persistência dos valores retrocompatíveis no WorldConfig.
- O renderizador preserva os presets Nebula, Galaxy, Aurora, Japanese, Crystal e Sakura, as partículas e o sistema de clima.
- Apenas componentes Java 8, Forge 1.8.9/LWJGL 2. Nenhuma textura, dependência ou executável externo introduzido.

## Limites da primeira fase

**Não é** um pipeline HDR/EXR, um sistema de shaders ou uma ferramenta de modelagem 3D. Uma imagem `*.cube.png` possui todas as faces numa única textura, não seis arquivos independentes. A orientação do atlas é explícita no [guia de importação](IMPORTS.md).

Os valores default preservam o aspecto anterior. As configurações não mexem no relógio do servidor, na chuva real ou na orientação do jogador.

### Validação recomendada dentro do Minecraft

1. Compile/teste as edições Legit e Full com Java 8. O workflow de CI cobre JUnit e integridade do JAR; não atesta visual sem um smoke gráfico real.
2. Compare a imagem PNG equiretangular importada antes/depois, com os campos novos em seus defaults.
3. Importe um atlas `meu-ceu.cube.png` (exemplo: 1536×1024), valide cada direção e as costuras.
4. Experimente yaw, drift, brilho e cintilação; confirme que as opções persistem após reiniciar.
5. Ative o céu customizado nos render distances 2 e 4, confirme que os terrenos continuam aparecendo.
6. Teste restauração do céu vanilla, dim. Nether/End, presets e perfil de servidor.
7. Valide a compatibilidade com OptiFine separadamente antes de lançar.

Screenshots antigos no repositório representam o código anterior e não comprovam a atualização.
