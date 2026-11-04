package dev.alien.client.cosmetic.mesh;

/**
 * Reusable fabric stream. Updates the curved grid in place without frame allocations.
 * Shoulder vertices are pinned; waves increase gradually towards the rounded hem.
 * One instance may be updated and drawn sequentially for several players on the render thread.
 */
public final class CapeCloth implements MeshView {
    private final int rows, columns;
    private final float[] positions, normals;
    private final int[] vertices;
    private final boolean[] reverse;

    public CapeCloth() { this(14, 6); }
    /** Resolution order is longitudinal rows, then horizontal columns. */
    public CapeCloth(int rows, int columns) {
        if (rows < 4 || rows > 40 || columns < 2 || columns > 24) throw new IllegalArgumentException("Cape grid must be 4..40 rows by 2..24 columns");
        this.rows=rows; this.columns=columns;
        int grid=(rows+1)*(columns+1);
        positions=new float[grid*3]; normals=new float[grid*3];
        vertices=new int[rows*columns*12]; reverse=new boolean[vertices.length];
        int at=0;
        for(int row=0;row<rows;row++)for(int col=0;col<columns;col++) {
            int a=index(row,col),b=index(row,col+1),c=index(row+1,col+1),d=index(row+1,col);
            int[] cell={a,b,c,a,c,d,c,b,a,d,c,a};
            for(int i=0;i<12;i++){vertices[at]=cell[i];reverse[at++]=i>=6;}
        }
        update(0,0,1);
    }

    public int rows() { return rows; }
    public int columns() { return columns; }

    /** ageTicks may include partial ticks; movement is a local visual speed, not player state. */
    public void update(float ageTicks,float movement,float flutter) {
        update(ageTicks,movement,flutter,0,0);
    }

    public void update(float ageTicks,float movement,float flutter,float bend,float lateral) {
        double age=Float.isFinite(ageTicks)?ageTicks%20000f:0;
        double speed=finiteClamp(movement,0,2),wind=finiteClamp(flutter,0,2);
        double sway=Math.sin(age*.055)*wind*.35;
        for(int row=0;row<=rows;row++) {
            double t=row/(double)rows,t2=t*t;
            double half=4.65+.65*Math.sin(t*Math.PI)-.85*t;
            for(int col=0;col<=columns;col++) {
                double s=col/(double)columns*2-1;
                double phase=age*.14-t*4+s*1.5;
                int at=index(row,col)*3;
                positions[at]=(float)(s*half+sway*t2+finiteClamp(lateral,-2,2)*t2);
                positions[at+1]=(float)(1+16*t-.65*Math.pow(Math.abs(s),5)*Math.pow(t,7)
                        +.18*Math.pow(Math.sin(s*Math.PI*3),2)*Math.pow(t,8));
                positions[at+2]=(float)(3.15+t*.35+t2*(.75+speed*.9)
                        +finiteClamp(bend,0,5)*t2+wind*.65*t2*Math.sin(phase)+wind*.20*t2*Math.sin(age*.095+s*4)
                        +wind*.12*Math.sin(s*Math.PI*5)*t2);
            }
        }
        // Smooth vertex normals from adjacent grid tangents, rather than flat rectangular panels.
        for(int row=0;row<=rows;row++)for(int col=0;col<=columns;col++) {
            int left=index(row,Math.max(0,col-1))*3,right=index(row,Math.min(columns,col+1))*3;
            int upper=index(Math.max(0,row-1),col)*3,lower=index(Math.min(rows,row+1),col)*3;
            double ux=positions[right]-positions[left],uy=positions[right+1]-positions[left+1],uz=positions[right+2]-positions[left+2];
            double vx=positions[lower]-positions[upper],vy=positions[lower+1]-positions[upper+1],vz=positions[lower+2]-positions[upper+2];
            double nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx,length=Math.sqrt(nx*nx+ny*ny+nz*nz);
            int at=index(row,col)*3;
            if(length<1e-8){normals[at]=0;normals[at+1]=0;normals[at+2]=1;}
            else{normals[at]=(float)(nx/length);normals[at+1]=(float)(ny/length);normals[at+2]=(float)(nz/length);}
        }
    }

    private int index(int row,int column){return row*(columns+1)+column;}
    private static double finiteClamp(float value,double min,double max){return Float.isFinite(value)?Math.max(min,Math.min(max,value)):min;}
    private int offset(int vertex){return vertices[vertex]*3;}
    @Override public int vertexCount(){return vertices.length;}
    @Override public float x(int i){return positions[offset(i)];}
    @Override public float y(int i){return positions[offset(i)+1];}
    @Override public float z(int i){return positions[offset(i)+2];}
    @Override public float nx(int i){return normals[offset(i)]*(reverse[i]?-1:1);}
    @Override public float ny(int i){return normals[offset(i)+1]*(reverse[i]?-1:1);}
    @Override public float nz(int i){return normals[offset(i)+2]*(reverse[i]?-1:1);}
    @Override public int material(int i){return reverse[i]?Mesh.SHADOW:Mesh.MAIN;}
    @Override public float shade(int i){return Mesh.light(nx(i),ny(i),nz(i));}
    /** Texture coordinates remain pinned to the cloth while its positions deform. */
    public float u(int vertex){return vertices[vertex]%(columns+1)/(float)columns;}
    public float v(int vertex){return vertices[vertex]/(columns+1)/(float)rows;}
}
