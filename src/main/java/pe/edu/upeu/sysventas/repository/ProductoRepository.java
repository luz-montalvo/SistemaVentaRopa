package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.enums.TipoProducto;
import pe.edu.upeu.sysventas.model.Categoria;
import pe.edu.upeu.sysventas.model.Marca;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.model.UnidMedida;

public class ProductoRepository extends AbstractJpaRepository<Producto, Long>{
    private long sequence=1;
    @Override
    protected Long getId(Producto entity) {
        return entity.getIdProducto();
    }

    @Override
    protected void setId(Producto entity, Long id) {
        entity.setIdProducto(id);
    }

    @Override
    protected Long generateId() {
        return sequence++;
    }

    public void seedData() {
        if (findAll().isEmpty()) {

            Categoria c=new Categoria();
            c.setIdCategoria(2L);

            Marca m=new Marca();
            m.setIdMarca(1L);

            UnidMedida u=new UnidMedida();
            u.setIdUnidad(1L);

            save(new Producto(generateId(), "Telivisor", TipoProducto.PRODUCTO,
                    1200.00, 0.00, 100.00,12.0,0.0, c,m,u ));
        }
    }

}
