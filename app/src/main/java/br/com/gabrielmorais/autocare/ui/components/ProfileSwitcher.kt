package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Profile

/**
 * Avatar do perfil ativo na barra superior. O toque abre a lista de perfis para
 * trocar de dono sem sair da tela, e um atalho para gerencia-los.
 */
@Composable
fun ProfileSwitcher(
  active: Profile?,
  profiles: List<Profile>,
  onSelect: (Profile) -> Unit,
  onManage: () -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Box {
    IconButton(onClick = { expanded = true }) {
      ProfileAvatar(
        photo = rememberLocalPhoto(active?.photoPath),
        size = 32.dp,
        contentDescription = stringResource(R.string.content_desc_switch_profile, active?.name.orEmpty())
      )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      profiles.forEach { profile ->
        DropdownMenuItem(
          text = { Text(profile.name) },
          leadingIcon = { ProfileAvatar(photo = rememberLocalPhoto(profile.photoPath), size = 28.dp) },
          trailingIcon = {
            if (profile.id == active?.id) {
              Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.content_desc_active_profile))
            }
          },
          onClick = {
            expanded = false
            onSelect(profile)
          }
        )
      }
      HorizontalDivider()
      DropdownMenuItem(
        text = { Text(stringResource(R.string.profiles_manage)) },
        leadingIcon = { Icon(Icons.Outlined.ManageAccounts, contentDescription = null) },
        onClick = {
          expanded = false
          onManage()
        }
      )
    }
  }
}
