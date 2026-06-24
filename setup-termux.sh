#!/bin/bash
# =============================================================
# setup-termux.sh — Script de démarrage pour Termux
# Lance ce script UNE SEULE FOIS pour configurer le projet
# =============================================================

set -e
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}=== MEPC Montréal — Setup Termux ===${NC}"

# 1. Installer les dépendances Termux
echo -e "\n${YELLOW}[1/5] Installation des paquets Termux...${NC}"
pkg update -y && pkg install -y git openjdk-17 gh

# 2. Vérifier Java
echo -e "\n${YELLOW}[2/5] Vérification Java...${NC}"
java -version
echo "JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))"
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))

# 3. Générer le keystore
echo -e "\n${YELLOW}[3/5] Génération du keystore de signature...${NC}"
echo -e "${RED}Retiens bien ces mots de passe !${NC}"

read -p "Mot de passe du keystore: " KS_PASS
read -p "Alias de la clé (ex: mepc): " KEY_ALIAS
read -p "Mot de passe de la clé: " KEY_PASS

keytool -genkeypair \
  -v \
  -keystore mepc-keystore.jks \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass "$KS_PASS" \
  -keypass "$KEY_PASS" \
  -dname "CN=MEPC Montreal, OU=Android, O=MEPC, L=Montreal, S=QC, C=CA"

echo -e "${GREEN}Keystore créé : mepc-keystore.jks${NC}"

# 4. Encoder en base64 pour GitHub Secrets
echo -e "\n${YELLOW}[4/5] Encodage Base64 du keystore...${NC}"
KS_BASE64=$(base64 -w 0 mepc-keystore.jks)

echo -e "\n${GREEN}=== AJOUTE CES 4 SECRETS DANS GITHUB ===${NC}"
echo -e "Repo > Settings > Secrets and variables > Actions > New repository secret\n"
echo -e "${YELLOW}KEYSTORE_BASE64${NC}"
echo "$KS_BASE64"
echo ""
echo -e "${YELLOW}KEYSTORE_PASSWORD${NC}"
echo "$KS_PASS"
echo ""
echo -e "${YELLOW}KEY_ALIAS${NC}"
echo "$KEY_ALIAS"
echo ""
echo -e "${YELLOW}KEY_PASSWORD${NC}"
echo "$KEY_PASS"

# 5. Push sur GitHub
echo -e "\n${YELLOW}[5/5] Push sur GitHub...${NC}"
read -p "URL de ton repo GitHub (ex: https://github.com/ton-user/mepc-app): " REPO_URL

git init
git add .
git commit -m "Initial: MEPC Montréal native Android app"
git branch -M main
git remote add origin "$REPO_URL"
git push -u origin main

echo -e "\n${GREEN}=== TERMINÉ ! ===${NC}"
echo "GitHub Actions va maintenant compiler ton APK."
echo "Va dans: $REPO_URL/actions pour suivre le build."
echo "L'APK sera dans: $REPO_URL/releases"
