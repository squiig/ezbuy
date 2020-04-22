#
# bump.sh
# author: cerrealic

function bump() {
	source ~/color_vars.sh

	local app_dir=;
	local plugin_yml_file=${app_dir}src/main/resources/plugin.yml
	local gradle_build_file=${app_dir}build.gradle
	local cache_file=.bump_cache
	local -i major=0
	local -i minor=0
	local -i patch=0
	local suffix=''
	local is_snapshot=false

	resetOptInd() {
		OPTIND=1
	}

	usage() {
		echo -e "${RED}Usage: bump (-a|-c|-S <version>) | (-j|-J, -m|-M, -p|-P [-s <suffix>])${NC}"
		echo
		echo -e "${YELLOW}Flag execution order is as given!${NC}"
		echo
		echo -e "Flag descriptions:\n-a ${DGRAY}:${NC} Apply what's currently cached\n-c ${DGRAY}:${NC} Show what's currently cached\n-S ${DGRAY}:${NC} Bump to a specific version\n-j|-J ${DGRAY}:${NC} Bump the major index (capital for specific)\n-m|-M ${DGRAY}:${NC} Bump the minor index (capital for specific)\n-p|-P ${DGRAY}:${NC} Bump the patch index (capital for specific)\n-s ${DGRAY}:${NC} Add a suffix (Options: ${GREEN}alpha, beta, release, snapshot${NC})"
	}

	# Flags are allowed, anything else isn't
	case $@ in
		-*) ;;
		*) usage; return 1 ;;
	esac

	createCache() {
		echo ${major} >> ${cache_file}
		echo ${minor} >> ${cache_file}
		echo ${patch} >> ${cache_file}
		echo ${suffix} >> ${cache_file}
		echo ${is_snapshot} >> ${cache_file}
	}

	saveCache() {
		sed -i "1s/.*/$major/" ${cache_file}
		sed -i "2s/.*/$minor/" ${cache_file}
		sed -i "3s/.*/$patch/" ${cache_file}
		sed -i "4s/.*/$suffix/" ${cache_file}
		sed -i "5s/.*/$is_snapshot/" ${cache_file}
	}

	loadCache() {
		if ! [ -f ${cache_file} ]; then
			echo -e "${YELLOW}Cache does not exist yet, creating one now...${NC}"
			createCache
		fi

		major=$(sed -n 1p ${cache_file})
		minor=$(sed -n 2p ${cache_file})
		patch=$(sed -n 3p ${cache_file})
		suffix=$(sed -n 4p ${cache_file})
		is_snapshot=$(sed -n 5p ${cache_file})
	}

	# Every time we terminate the shell session the local vars get lost,
	# so we load the cache to pick up where we left off.
	loadCache

	bumpSpecific() {
		# plugin.yml
		if sed -i "s/^version:.*$/version: $1/" ${plugin_yml_file}; then
			echo -e "${GREEN}Bumped ${LGREEN}plugin.yml${GREEN} version to ${LGREEN}$1${NC}"
		else
			echo -e "${RED}Failed to bump plugin.yml to $1${NC}"
			return 1
		fi

		# build.gradle
		if sed -i "s/^version [\'\"].*[\'\"]$/version \'$1\'/" ${gradle_build_file}; then
			echo -e "${GREEN}Bumped ${LGREEN}build.gradle${GREEN} version to ${LGREEN}$1${NC}"
		else
			echo -e "${RED}Failed to bump build.gradle to $1${NC}"
			return 1
		fi

		return 0
	}

	apply() {
		local spec="$major.$minor.$patch$suffix"

		if [ "$is_snapshot" = true ]; then
			spec="$major.$minor.$patch$suffix-SNAPSHOT"
		fi

		if bumpSpecific ${spec}; then
			saveCache
			return 0
		fi

		return 1
	}

	while getopts "acS:J:M:P:jmps:" flag; do
		case ${flag} in
			a)
				if apply; then
					if [ "$is_snapshot" = true ]; then
					echo -e "${GREEN}Applied ${LGREEN}$major.$minor.$patch$suffix-SNAPSHOT${GREEN}...${NC}"
					else
					echo -e "${GREEN}Applied ${LGREEN}$major.$minor.$patch$suffix${GREEN}...${NC}"
					fi
					resetOptInd
					return 0
				fi
				return 1
			;;
			c)
				loadCache
				if [ "$is_snapshot" = true ]; then
					echo "$major.$minor.$patch$suffix-SNAPSHOT"
				else
					echo "$major.$minor.$patch$suffix"
				fi
				resetOptInd
				return 0
			;;
			S)
				if [ $OPTARG=":" ]; then
					usage
					return 1
				fi

				if bumpSpecific $OPTARG; then
					resetOptInd
					return 0
				fi

				return 1
			;;
			J) major=$OPTARG ;;
			M) minor=$OPTARG ;;
			P) patch=$OPTARG ;;
			j)
				echo -e "${GREEN}Incrementing ${LGREEN}major${GREEN} index...${NC}"
				major=$(($major+1))
			;;
			m)
				echo -e "${GREEN}Incrementing ${LGREEN}minor${GREEN} index...${NC}"
				minor=$(($minor+1))
			;;
			p)
				echo -e "${GREEN}Incrementing ${LGREEN}patch${GREEN} index...${NC}"
				patch=$(($patch+1))
			;;
			s)
				case $OPTARG in
					alpha)
						echo -e "${GREEN}Setting ${LGREEN}alpha${GREEN} suffix...${NC}"
						suffix='a'
					;;
					beta)
						echo -e "${GREEN}Setting ${LGREEN}beta${GREEN} suffix...${NC}"
						suffix='b'
					;;
					release)
						echo -e "${GREEN}Setting ${LGREEN}release${GREEN} suffix... (defaults to none)${NC}"
						suffix=;
					;;
					snapshot)
						echo -e "${GREEN}Adding ${LGREEN}-SNAPSHOT${GREEN} suffix...${NC}"
						is_snapshot=true
					;;
					--)
						echo -e "${GREEN}Removing suffix...${NC}"
						suffix='';
						is_snapshot=false
					;;
				esac
			;;
			?) usage; return 1 ;;
		esac
	done

	apply
	resetOptInd
}