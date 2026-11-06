import React, { useState, useEffect } from 'react';

const AdminSettings = () => {
    // 複数のフラグを管理できるようにオブジェクトで定義
    const [config, setConfig] = useState({
        grade_display: false,
        scholarship_display: false
    });

    // 1. 画面を開いた時にJava(8080)から現在の設定を取得
    useEffect(() => {
        fetch('http://localhost:8080/api/admin/config')
            .then(res => res.json())
            .then(data => {
                setConfig(data);
                console.log("設定を取得しました:", data);
            })
            .catch(err => console.error("データ取得失敗:", err));
    }, []);

    // 2. スイッチが押された時にJava(8080)のPATCH APIを叩く
    const handleToggle = async (key) => {
        const nextStatus = !config[key];

        try {
            const response = await fetch(
                `http://localhost:8080/api/admin/config/${key}?status=${nextStatus}`,
                { method: 'PATCH' }
            );

            if (response.ok) {
                // 成功したらReact側の表示(state)も更新
                setConfig(prev => ({ ...prev, [key]: nextStatus }));
            }
        } catch (error) {
            alert("更新に失敗しました。Javaサーバーが動いているか確認してください。");
        }
    };

    return (
        <div style={{ padding: '20px', border: '1px solid #ccc', borderRadius: '8px', maxWidth: '400px' }}>
            <h3>🛠 システム管理設定</h3>
            <hr />
            <div style={{ marginTop: '15px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
                    <span>成績表示機能:</span>
                    <button
                        onClick={() => handleToggle('grade_display')}
                        style={{ backgroundColor: config.grade_display ? '#4caf50' : '#f44336', color: 'white', border: 'none', padding: '5px 15px', borderRadius: '4px', cursor: 'pointer' }}
                    >
                        {config.grade_display ? "公開中" : "停止中"}
                    </button>
                </div>

                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>奨学金表示機能:</span>
                    <button
                        onClick={() => handleToggle('scholarship_display')}
                        style={{ backgroundColor: config.scholarship_display ? '#4caf50' : '#f44336', color: 'white', border: 'none', padding: '5px 15px', borderRadius: '4px', cursor: 'pointer' }}
                    >
                        {config.scholarship_display ? "公開中" : "停止中"}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AdminSettings;