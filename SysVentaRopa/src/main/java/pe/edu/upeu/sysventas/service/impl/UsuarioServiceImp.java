package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.UsuarioRepository;
import pe.edu.upeu.sysventas.service.IUsuarioService;
import pe.edu.upeu.sysventas.utils.PasswordUtil;

import java.util.Optional;

public class UsuarioServiceImp extends CrudGenericoServiceImp<Usuario, Long> implements IUsuarioService {
    private final UsuarioRepository repo;

    public UsuarioServiceImp(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    protected ICrudGenericoRepository<Usuario, Long> getRepo() {
        return repo;
    }

    /** Al registrar, la clave se guarda encriptada (nunca en texto plano). */
    @Override
    public Usuario save(Usuario u) {
        u.setClave(PasswordUtil.encriptar(u.getClave()));
        return super.save(u);
    }

    /** Al editar: si dejó la clave vacía se mantiene la anterior; si escribió una nueva, se encripta. */
    @Override
    public Usuario update(Long id, Usuario u) {
        if (u.getClave() == null || u.getClave().isEmpty()) {
            u.setClave(findById(id).getClave());
        } else {
            u.setClave(PasswordUtil.encriptar(u.getClave()));
        }
        return super.update(id, u);
    }

    /** Login: busca por nombre de usuario y compara el hash de la clave escrita. */
    @Override
    public Optional<Usuario> loginUsuario(String usuario, String clave) {
        return repo.findByUsuario(usuario)
                .filter(u -> PasswordUtil.verificar(clave, u.getClave()));
    }

    @Override
    public boolean existeUsuario(String usuario, Long idExcluir) {
        return repo.existeUsuario(usuario, idExcluir);
    }

    /**
     * Se ejecuta al iniciar la app: las claves que aún estén en texto plano
     * (usuarios de prueba del script SQL o de versiones anteriores) se encriptan.
     */
    @Override
    public void encriptarClavesPendientes() {
        for (Usuario u : repo.findAll()) {
            if (!PasswordUtil.estaEncriptada(u.getClave())) {
                repo.actualizarClave(u.getIdUsuario(), PasswordUtil.encriptar(u.getClave()));
            }
        }
    }
}
