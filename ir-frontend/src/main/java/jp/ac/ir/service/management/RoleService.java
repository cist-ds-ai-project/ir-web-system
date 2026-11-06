package jp.ac.ir.service.management;

import jp.ac.ir.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    // すべてのロールをMap型で取得
    // キー: display_name, 値: id
    public Map<String, Integer> getAllRolesMap(){
        HashMap<String, Integer> allRolesMap = new HashMap<>();
        for(Role role : roleRepository.getAllRolesList())
            allRolesMap.put(role.display_name(), role.id());
        return allRolesMap;
    }

    // user_idに応じたロールのリストを取得
    public List<Role> getRoleList(long userId){
        List<Role> roleList = roleRepository.getRoleList(userId);
        return roleList;
    }

    // roleの名前(display_name)からrole_idを取得(csvファイルでの追加用)
    public Optional<Integer> getRoleId(String roleName){
        return roleRepository.getRoleId(roleName);
    }

    // 登録されているroleの取得
    public List<String> getRoles(){return roleRepository.getRole();}
}
